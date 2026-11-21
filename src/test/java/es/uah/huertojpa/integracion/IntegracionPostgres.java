package es.uah.huertojpa.integracion;

import com.jayway.jsonpath.JsonPath;
import es.uah.huertojpa.seguridad.LimiteIntentosLogin;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.condition.EnabledIf;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.PostgreSQLContainer;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * P8 – Base de los tests de integración: arranca la aplicación completa (cadena de filtros de seguridad real, Flyway
 * V1..Vn y Hibernate {@code validate}) contra un <b>PostgreSQL 15 en Docker</b>, la misma versión que la Raspberry.
 * <p>
 * Un único contenedor para todas las clases (patrón «singleton container»): Spring reutiliza el contexto entre clases
 * y el contenedor lo para Ryuk al acabar la JVM. Si no hay Docker (p. ej. en un equipo sin él) los tests se
 * <b>saltan</b> en vez de fallar. Cada test parte de los datos de {@code integracion/datos.sql}.
 */
@SpringBootTest(properties = {
        "device.api-key=" + IntegracionPostgres.CLAVE_DISPOSITIVO,
        // 48 bytes a cero en Base64: clave fija solo para los tests
        "jwt.secret=AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
        "spring.datasource.hikari.maximum-pool-size=3",
        // Las alertas se revisan a mano en los tests (AlertasIntegracionTest), no con el planificador
        "alertas.activas=false",
        // S2: BCrypt con el coste mínimo para que los muchos logins de los tests no tarden
        "contrasenas.bcrypt-coste=4"
})
@AutoConfigureMockMvc
@EnabledIf(value = "es.uah.huertojpa.integracion.IntegracionPostgres#dockerDisponible",
        disabledReason = "Docker no disponible: se saltan los tests de integración con PostgreSQL")
@Sql("/integracion/datos.sql")
public abstract class IntegracionPostgres {
    public static final String CLAVE_DISPOSITIVO = "clave-esp32-de-prueba";
    /** Ids de {@code datos.sql}. */
    protected static final int ADMIN = 101, ANA = 102, LUIS = 103;
    protected static final int HUERTO_ANA = 105, HUERTO_LUIS = 106;
    protected static final int MACETA_ANA = 110, MACETA_LUIS = 111;
    protected static final int PLANTA_ANA = 107, SENSOR_ANA = 120, SENSOR_LUIS = 121, SENSOR_LIBRE = 122;
    protected static final int DEPOSITO_ANA = 130;

    @ServiceConnection
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("huertodb").withUsername("huerto").withPassword("huerto");

    static {
        if (dockerDisponible()) {
            POSTGRES.start();
        }
    }

    public static boolean dockerDisponible() {
        try {
            return DockerClientFactory.instance().isDockerAvailable();
        } catch (Throwable e) {
            return false;
        }
    }

    @Autowired
    protected MockMvc mvc;
    @Autowired
    private LimiteIntentosLogin limiteIntentos;

    /** S6: el contexto (y el limitador, en memoria) se comparte entre clases; los fallos de un test no cuentan en otro. */
    @BeforeEach
    void olvidarIntentosDeLogin() {
        limiteIntentos.reiniciar();
    }

    /**
     * Login real por {@code /auth/login} (la contraseña de datos.sql es el propio nombre de usuario, guardada «a la
     * antigua», sin BCrypt: el primer login la convierte).
     */
    protected String token(String usuario) throws Exception {
        String json = mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userName\":\"" + usuario + "\",\"password\":\"" + usuario + "\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(json, "$.token");
    }

    protected String bearer(String usuario) throws Exception {
        return "Bearer " + token(usuario);
    }
}
