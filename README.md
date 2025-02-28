
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

