
# GestioPlantasBackend

**Descripción:**
GestioPlantasBackend es un servidor web diseñado para proporcionar servicios de control y gestión de huertos automatizados. Este backend facilita la monitorización y administración eficiente de cultivos mediante la integración de tecnologías modernas.

## Características

- **Monitorización en Tiempo Real:** Supervisa las condiciones del huerto, incluyendo humedad, temperatura y otros parámetros ambientales.
- **Control Automatizado:** Gestiona sistemas de riego, iluminación y otros dispositivos automatizados según las necesidades del cultivo.
- **Gestión de Usuarios:** Permite la creación y administración de perfiles de usuarios con diferentes niveles de acceso.
- **Alertas y Notificaciones:** Envía notificaciones en caso de condiciones anómalas o eventos predefinidos.

## Tecnologías Utilizadas

- **Lenguaje de Programación:** Java
- **Framework:** Spring Boot
- **Gestión de Dependencias:** Maven
- **Contenerización:** Docker

## Estructura del Proyecto

El proyecto sigue la estructura estándar de una aplicación Spring Boot:

- `src/main/java`: Contiene el código fuente de la aplicación.
- `src/main/resources`: Archivos de configuración y recursos estáticos.
- `src/test`: Pruebas unitarias y de integración.

## Requisitos Previos

- **Java 11** o superior instalado.
- **Maven** para la gestión de dependencias.
- **Docker** (opcional, para despliegue en contenedores).

## Instalación y Ejecución

1. **Clonar el repositorio:**

   ```bash
   git clone https://github.com/carlosGJAlcala/GestioPlantasBackend.git
   cd GestioPlantasBackend
   ```

2. **Construir el proyecto con Maven:**

   ```bash
   ./mvnw clean install
   ```

3. **Ejecutar la aplicación:**

   ```bash
   ./mvnw spring-boot:run
   ```

   La aplicación estará disponible en `http://localhost:8080`.

## Despliegue con Docker

Para desplegar la aplicación utilizando Docker:

1. **Construir la imagen de Docker:**

   ```bash
   docker build -t gestioplantas-backend .
   ```

2. **Ejecutar el contenedor:**

   ```bash
   docker run -p 8080:8080 gestioplantas-backend
   ```

## Contribuciones

Las contribuciones son bienvenidas. Si deseas colaborar:

1. Realiza un fork del repositorio.
2. Crea una nueva rama para tus cambios.
3. Envía un pull request detallando las modificaciones propuestas.

## Licencia

Este proyecto se distribuye bajo la licencia MIT. Para más detalles, consulta el archivo `LICENSE` en el repositorio.


## Configuración y seguridad (2026-11-05)

Los secretos ya no están en el código: se leen de variables de entorno (plantilla en [`.env.example`](.env.example)).

| Variable | Uso | Si falta |
|---|---|---|
| `DB_URL`, `DB_USER`, `DB_PASSWORD` | PostgreSQL | `jdbc:postgresql://192.168.1.33:5432/huertodb`, `huerto`, sin clave |
| `DB_MIGRAR`, `DB_VALIDAR` | Flyway y validación del esquema por Hibernate | `true`, `validate` (con MySQL: `false`, `none`) |
| `MAIL_USERNAME`, `MAIL_PASSWORD` | Gmail (contraseña de aplicación) | el envío de correo falla |
| `JWT_SECRET` | firma de los tokens (Base64, ≥ 32 bytes: `openssl rand -base64 48`) | clave aleatoria: las sesiones se pierden al reiniciar |
| `DEVICE_API_KEY` | clave del ESP32 (cabecera `X-Device-Key`) | endpoints `/auth/**` abiertos (modo compatibilidad, aviso en el log) |
| `CORS_ORIGINS` | orígenes del front, separados por comas | `*` |
| `CONTRASENAS_COSTE`, `CONTRASENAS_MIGRAR` | coste de BCrypt y conversión al arrancar de las contraseñas antiguas | `10`, `true` |

### Contraseñas (2026-11-11, S2)

- El front sigue mandando en el login el **SHA-256** de la contraseña (no cambia el API); el backend guarda
  **`BCrypt(sha256)`** con sal (`seguridad/Contrasenas`). Antes guardaba el SHA-256 tal cual: quien leyera la tabla
  `persona` (o `GET /persona/...`, que lo devolvía) podía entrar directamente con ese valor.
- Las contraseñas antiguas siguen valiendo: se convierten a BCrypt **al arrancar** (`MigracionContrasenas`) y, si
  alguna se mete a mano después (`bd/02-crear-admin.sql`), en su **primer login correcto**.
- `PUT /persona/contrasena` con `{"actual": sha256, "nueva": sha256}`: cada uno cambia la suya (400 si la actual no es
  correcta). `PUT /persona` ya **no** cambia la contraseña.
- El JSON de `persona` ya no incluye `passwordSHA256` ni `password` (solo se lee en el alta).

### Intentos de login y contraseñas restablecidas (2026-11-14, S6/S8)

- **Límite de intentos** (`seguridad/LimiteIntentosLogin`, en memoria): 5 fallos del mismo usuario desde la misma IP
  en 15 min, o 20 fallos desde la misma IP con cualquier usuario ⇒ `POST /auth/login` responde **429** con
  `Retry-After` y `reintentarEnSegundos`, sin mirar la contraseña. El bloqueo dura 15 min y se duplica si se repite
  (máx. 24 h). No se bloquea a un usuario para todo el mundo: desde otra IP puede entrar. Variables `LOGIN_INTENTOS`,
  `LOGIN_INTENTOS_IP`, `LOGIN_VENTANA`, `LOGIN_BLOQUEO`.
- La IP del cliente detrás del nginx de la Raspberry sale de `X-Forwarded-For` (`server.forward-headers-strategy=native`,
  que solo se fía de proxies de red privada).
- **Restablecer contraseña** (solo admin): `PUT /persona/{id}/contrasena` `{"nueva": "<sha256>"}`. Marca
  `debe_cambiar_contrasena` (el login devuelve `debeCambiarContrasena: true` y el front obliga a cambiarla) y quita los
  bloqueos de login de esa persona.
- Tras cualquier cambio de contraseña (`contrasena_cambiada`, V8) los **JWT emitidos antes dejan de valer**.
  `PUT /persona/contrasena` devuelve ahora `{"cambiada": true, "token": "…"}` para seguir en la sesión actual.
- `ultimo_acceso` (V8): último login correcto, visible en la lista de personas del admin.

### Contraseña temporal impuesta y cerrar sesiones (2026-11-17, S8b/S9)

- **La temporal solo sirve para cambiarla** (`seguridad/ContrasenaTemporalFilter`, tras el filtro JWT): con
  `debe_cambiar_contrasena` el token solo vale para `PUT /persona/contrasena` y `GET /persona/userName/{suyo}`; lo
  demás responde **403** `{"mensaje": "…", "debeCambiarContrasena": true}`. El login y el ESP32 no se ven afectados.
- **La temporal caduca** (`seguridad/ContrasenaTemporal`, `CONTRASENA_TEMPORAL_VALIDEZ`, `P3D` por defecto): pasado el
  plazo, el login responde `idUser: "-1"` con `temporalCaducada: true` (ni fallo ni acierto para el límite de
  intentos) y las sesiones abiertas con ella reciben **401**. El admin tiene que poner otra.
- **Cerrar sesión en todos los equipos**: `POST /persona/sesiones/cerrar` (la propia persona; devuelve
  `{"cerradas": true, "token": "…"}` para seguir en este equipo) y `POST /persona/{id}/sesiones/cerrar` (solo admin).
  Guarda `sesiones_cerradas` (V9); los JWT emitidos antes de esa hora o de `contrasena_cambiada` dejan de valer.

## Base de datos PostgreSQL (2026-11-06)

La BD principal es el PostgreSQL de la Raspberry (`192.168.1.33:5432`, base `huertodb`, usuario `huerto`).
El esquema lo crea y versiona **Flyway** al arrancar el backend
([`src/main/resources/db/migration/postgresql`](src/main/resources/db/migration/postgresql)):

| Migración | Contenido |
|---|---|
| `V1__esquema_inicial` | personas/roles, huertos, macetas, sensores, depósito, tipos de planta, plantas e histórico antiguo |
| `V2__riego_y_ordenes` | `riego` y `orden_riego` |
| `V3__lectura` | `lectura`: histórico numérico con fecha real (P-DB1) |
| `V4__catalogo_tipos_planta` | 10 tipos de planta con rangos orientativos |

Hibernate arranca con `ddl-auto=validate`: si una entidad no cuadra con las tablas, el backend no arranca y dice
qué falta. **Para cambiar el esquema, añade una `V5__...sql`; no edites una migración ya aplicada.**

Puesta en marcha (una sola vez), desde [`bd/`](bd):

1. `./montar-bd-raspberry.sh` — crea el usuario `huerto` y la base `huertodb` (pide la clave del superusuario
   `postgres` de la Raspberry) y muestra la clave generada para `DB_PASSWORD`.
2. Arrancar el backend con el `.env` → Flyway crea las tablas.
3. `02-crear-admin.sql` — primer administrador (instrucciones en el propio fichero).
4. Opcional: `migrar-desde-mysql.sh` — copia los datos de la BD MySQL antigua con `pgloader` y pasa el
   histórico `fechahora` a `lectura` (`03-despues-de-migrar.sql`).

Los scripts MySQL anteriores quedan en `src/main/resources/db/mysql-antiguo/` solo como referencia.

### Métricas

`GET /metricas/maceta/{id}?horas=168` (JWT; mismo control de acceso que los riegos): humedad actual y rango,
% del tiempo en rango, horas de estrés hídrico / exceso de agua, tasa de secado y **próximo riego estimado**,
temperatura media y horas fuera de rango, horas de luz suficiente al día, conexión de la placa y latencia /
% de caducidad de las órdenes «Regar ahora».

#### Panel en una sola petición (2026-11-16, W9)

`GET /metricas/panel?horas=24` (JWT de una persona; la clave del ESP32 no vale aquí): todo lo que pinta el panel
del front. Devuelve `generado` (hora del huerto), `horas`, `macetas` —una por maceta de los huertos del usuario
(todas si es admin), ordenadas por huerto e id, con `id`, `huertoIdhuerto`, `posicionMacetaenElHuerto`, `plantas`
(nombres) y `metricas` (lo mismo que `/metricas/maceta/{id}`, con `estado`)— y `alertas` (las abiertas, como
`GET /alertas`). El depósito se calcula una vez por huerto (también en la revisión de alertas cada 5 min).

### Serie de lecturas para gráficas (2026-11-09)

`GET /lectura/maceta/{id}?magnitud=HUMEDAD&desde=2026-11-02T00:00:00&hasta=2026-11-09T00:00:00&paso=60`
(JWT; solo la dueña de la maceta o el admin). Agrega las lecturas en intervalos fijos con `date_bin`
(**PostgreSQL ≥ 14**): una entrada por intervalo con datos (`ts`, `media`, `minimo`, `maximo`, `lecturas`) y el
rango ideal de la planta (`rangoMinimo`/`rangoMaximo`). Por defecto, últimos 7 días; sin `paso`, el backend elige
el más fino que da ≤ 200 puntos (1, 5, 10, 15, 30 min, 1, 2, 3, 6, 12, 24 h). Rango máximo 90 días.
`magnitud` = `HUMEDAD`, `TEMPERATURA`, `LUZ`… (mayúsculas o minúsculas); valores raros o `desde ≥ hasta` → 400.

Desde esta entrega **todas las fechas del API salen en ISO-8601** (`"2026-11-08T10:00:00"`, `"2026-10-01"`) en vez
de arrays `[2026,11,8,10,0]` (se quitó `@EnableWebMvc`, S5/P7).

### Alertas (2026-11-10)

Cada 5 minutos (`ALERTAS_INTERVALO`, ISO-8601) el backend calcula el estado de cada maceta y abre o cierra alertas
en la tabla `alerta` (V6). Criterios (`alertas/EstadoMaceta`, los mismos que pinta el panel en
`GET /metricas/maceta/{id}` → `estado`):

| Tipo | Nivel | Cuándo |
|---|---|---|
| `PLACA_DESCONECTADA` | crítico | Ninguna lectura en los últimos 30 min (mientras, no se evalúan humedad ni temperatura). |
| `RIEGO_FALLIDO` | crítico / aviso | Último riego `DEPOSITO_VACIO` (crítico), `LIMITE_PULSOS`, `SENSOR_ERROR`, o bomba activada sin subir la humedad. |
| `TIERRA_SECA` / `EXCESO_AGUA` | aviso | Última humedad fuera del rango del tipo de planta. |
| `TEMPERATURA_FUERA_RANGO` | aviso | Última temperatura fuera del rango del tipo de planta. |

- Una sola alerta abierta por maceta y tipo (índice único parcial); se cierra cuando lleva `ALERTAS_ESPERA_CIERRE`
  (30 min) sin verse, para no abrir y cerrar si la humedad baila en el umbral.
- **Un correo por alerta** a los usuarios del huerto (`spring-mail`, `MAIL_*`): las críticas al momento, los avisos
  si duran `ALERTAS_ESPERA_AVISO` (30 min; la tierra seca unos minutos antes de que riegue el ESP32 es normal).
  Sin `MAIL_PASSWORD` no se envía nada y las alertas solo se ven en el panel.
- `GET /alertas` (abiertas del usuario del token; todas si es admin) y `GET /alertas/maceta/{id}?dias=7` (historial).
- `ALERTAS_ACTIVAS=false` desactiva la revisión periódica.

#### Depósito y preferencias de aviso (2026-11-13, A2/A3)

- **`DEPOSITO_BAJO`**: el nivel del depósito sale de las lecturas del ultrasonidos en `lectura` (distancia en cm →
  % con `alturaDeposito`; mediana de las últimas lecturas). Aviso por debajo del 20 %, crítico en el 10 % (donde el
  ESP32 deja de regar). Solo con lecturas de los últimos 30 min. Si el huerto tiene varias macetas, todas muestran la
  alerta pero **el correo sale una vez**. Sustituye al antiguo correo «El deposito de agua esta vacio» de
  `DepositoAguaServiceImpl` (eliminado, igual que `IPersonaService.mandarEmail`).
- `GET /metricas/maceta/{id}` incluye `deposito`: `nivel`, `ultimaLectura`, `minutosDesdeLectura`,
  `consumoPorDia` (puntos/día desde el último rellenado) y `diasHastaVacio` (hasta el 10 %).
- **«Entendido»**: `POST /alertas/{id}/silenciar` — la alerta sigue abierta y en el panel, pero no se avisa por
  correo (campo `silenciada`). Si el problema se resuelve y vuelve, es una alerta nueva y sí avisa.
- **Preferencia por persona**: `avisosCorreo` en `persona` (V7, por defecto `true`). Se cambia con `PUT /persona`;
  si el campo no viene, se conserva. `PUT /persona` ahora también guarda el correo electrónico (antes lo ignoraba).

#### Depósito en litros y posible fuga (2026-11-18, A4/A5)

- `deposito` de las métricas añade `capacidadLitros` (= `cantidadMaxima` del depósito, en litros), `litros` que
  quedan y `consumoLitrosPorDia`. Sin capacidad puesta, los tres van a `null`.
- **`GET /lectura/deposito/huerto/{id}`** (`desde`, `hasta`, `paso` como en `/lectura/maceta/{id}`): serie del nivel
  del depósito en %, agregada en SQL con la altura del depósito (descarta lecturas sin eco). La banda va del 20 % al
  100 %. **404** si el huerto no tiene depósito con ultrasonidos y altura. Solo el dueño del huerto o el admin.
- **`FUGA_DEPOSITO`** (aviso): el depósito ha bajado ≥ 8 puntos en al menos 3 h **sin ningún riego con bomba** en
  el huerto (`deposito.perdidaSinRiego` y `horasSinRiego`). Se mira lo de las últimas 6 h, desde el último riego
  + 15 min y sin los 10 últimos minutos (un ciclo en curso aún no está registrado), con medias por hora y a partir
  del último rellenado. Como `DEPOSITO_BAJO`, es del huerto: un correo aunque lo vean varias macetas.

## Tests (2026-11-07)

```bash
mvn test        # unitarios + integración
```

- Los tests de integración (`src/test/java/.../integracion`, y `contextLoads`) arrancan la aplicación completa contra
  un **PostgreSQL 15 en Docker** con Testcontainers: aplican las migraciones Flyway, Hibernate `validate`, la cadena
  de seguridad real (JWT y `X-Device-Key`) y prueban el control de propiedad de cada recurso y el ciclo «Regar ahora».
  Necesitan Docker; si no hay, se saltan. Los datos de partida están en `src/test/resources/integracion/datos.sql`.
- Testcontainers está fijado a la **1.21.4**: la 1.19 que trae Spring Boot 3.3.1 no funciona con Docker Engine ≥ 29.

### Permisos (S4b)

| Quién | Qué puede hacer |
|---|---|
| Administrador (fila en `administrador`) | Todo, incluidos los listados globales (`GET /maceta`, `/planta`, `/sensor`, `/persona`…) y el catálogo `tipoPlanta`. |
| Usuario | Solo sus huertos (`huerto_has_usuario`) y lo que cuelga de ellos: macetas, plantas, depósitos, sensores asociados, riegos y métricas. Solo modifica su propia ficha de `persona`. Al crear un huerto queda asociado a él. |
| ESP32 (`X-Device-Key`) | Endpoints `/auth/**` (lecturas, riegos, órdenes). |

Un recurso ajeno o inexistente responde **403** (no se distingue para no revelar qué ids existen).
