-- P8: datos de partida de los tests de integración (se cargan antes de CADA test).
-- Ids altos para no chocar con las secuencias IDENTITY de lo que crean los propios tests.
-- Contraseña = nombre de usuario, guardada como en las filas antiguas (lo que manda el front, sin BCrypt): así cada
-- test pasa también por la conversión a BCrypt del primer login (S2).
TRUNCATE persona, huerto, sensor, fechahora, lectura, riego, orden_riego, alerta RESTART IDENTITY CASCADE;

INSERT INTO persona (id, nombre, usuario, passwordsha256, correoelectronico) VALUES
    (101, 'Admin', 'admin', 'admin', NULL),
    (102, 'Ana',   'ana',   'ana',   'ana@example.com'),
    (103, 'Luis',  'luis',  'luis',  NULL);
INSERT INTO administrador (persona_id) VALUES (101);
INSERT INTO usuario (persona_id, administrador_persona_id, fechadealta) VALUES
    (102, 101, now()), (103, 101, now());

INSERT INTO huerto (idhuerto, numeromacetas) VALUES (105, 1), (106, 1);
INSERT INTO huerto_has_usuario (huerto_idhuerto, usuario_persona_id) VALUES (105, 102), (106, 103);

INSERT INTO maceta (idmaceta, posicionmacetaenelhuerto, huerto_idhuerto) VALUES
    (110, 'balcon', 105), (111, 'terraza', 106);

INSERT INTO sensor (idsensor, nombresensor, magnitudamedir, cantidadmedida, unidades) VALUES
    (120, 'humedad-ana',  'Humedad', '65', '%'),
    (121, 'humedad-luis', 'Humedad', '50', '%'),
    (122, 'sin-asociar',  'Humedad', '0',  '%');
INSERT INTO maceta_has_sensor (maceta_idmaceta, sensor_idsensor) VALUES (110, 120), (111, 121);

INSERT INTO depositoagua (iddepositoagua, cantidadmaxima, alturadeposito, estado, huerto_idhuerto) VALUES
    (130, 5, 20, 'LLENO', 105);

-- Tipo 1 = tomate (catálogo de V4)
INSERT INTO planta (idplanta, nombreplanta, fechaplantacion, estado, tipoplanta_idtipoplanta, maceta_idmaceta) VALUES
    (107, 'Tomate de Ana',  DATE '2026-10-01', 'FELIZ', 1, 110),
    (108, 'Tomate de Luis', DATE '2026-10-01', 'FELIZ', 1, 111);
