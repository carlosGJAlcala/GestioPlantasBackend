package es.uah.huertojpa;

import es.uah.huertojpa.integracion.IntegracionPostgres;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * El contexto completo arranca contra PostgreSQL: Flyway aplica todas las migraciones e Hibernate {@code validate}
 * acepta las entidades. Antes necesitaba un MySQL local y no se ejecutaba nunca (por eso no se detectó en su día
 * que RiegoServiceImpl impedía arrancar la aplicación).
 */
class HuertojpaApplicationTests extends IntegracionPostgres {

	@Autowired
	Flyway flyway;

	@Test
	void contextLoads() {
		var info = flyway.info();
		assertEquals(0, info.pending().length, "no quedan migraciones pendientes");
		assertTrue(info.applied().length >= 4, "V1..V4 aplicadas");
	}
}
