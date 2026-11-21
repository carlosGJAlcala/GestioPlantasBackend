-- Tabla de riegos (entrega mejora-codigo 2026-11-04, backlog E7).
-- spring.jpa.hibernate.ddl-auto está desactivado: ejecutar a mano sobre huertodbdev.
CREATE TABLE IF NOT EXISTS riego (
    id              BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    maceta_id       INT          NOT NULL,
    ts              DATETIME     NOT NULL,
    duracion_ms     INT          NOT NULL DEFAULT 0,
    pulsos          INT          NOT NULL DEFAULT 0,
    humedad_antes   FLOAT        NULL,
    humedad_despues FLOAT        NULL,
    nivel_antes     FLOAT        NULL,
    nivel_despues   FLOAT        NULL,
    motivo          VARCHAR(10)  NOT NULL DEFAULT 'AUTO',
    resultado       VARCHAR(20)  NOT NULL DEFAULT 'OK',
    INDEX idx_riego_maceta_ts (maceta_id, ts),
    CONSTRAINT fk_riego_maceta FOREIGN KEY (maceta_id) REFERENCES maceta (IdMaceta) ON DELETE CASCADE
);
