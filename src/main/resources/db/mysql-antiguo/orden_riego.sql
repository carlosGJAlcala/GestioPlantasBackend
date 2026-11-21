-- Órdenes «Regar ahora» (entrega mejora-codigo 2026-11-05).
-- spring.jpa.hibernate.ddl-auto está desactivado: ejecutar a mano sobre huertodbdev.
CREATE TABLE IF NOT EXISTS orden_riego (
    id          BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    maceta_id   INT          NOT NULL,
    solicitante VARCHAR(45)  NULL,
    creada      DATETIME     NOT NULL,
    entregada   DATETIME     NULL,
    estado      VARCHAR(10)  NOT NULL DEFAULT 'PENDIENTE',
    INDEX idx_orden_maceta_estado (maceta_id, estado),
    CONSTRAINT fk_orden_maceta FOREIGN KEY (maceta_id) REFERENCES maceta (IdMaceta) ON DELETE CASCADE
);
