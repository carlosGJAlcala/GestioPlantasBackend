-- S6/S8 (entrega 2026-11-14).
-- S8: el administrador puede restablecer la contraseña de una persona. La que pone es temporal: en el siguiente login
-- el front obliga a cambiarla.
ALTER TABLE persona ADD COLUMN debe_cambiar_contrasena BOOLEAN NOT NULL DEFAULT FALSE;
-- S8: momento del último cambio de contraseña (por la persona o por el admin). Los JWT emitidos antes dejan de valer:
-- quien tuviera la sesión abierta con la contraseña antigua tiene que volver a entrar.
ALTER TABLE persona ADD COLUMN contrasena_cambiada TIMESTAMP;
-- Métrica para el admin: último login correcto (cuentas que no se usan, o actividad rara).
ALTER TABLE persona ADD COLUMN ultimo_acceso TIMESTAMP;
-- S6: los intentos fallidos se cuentan en memoria (seguridad/LimiteIntentosLogin), no en BD: un reinicio los olvida,
-- que es aceptable para una instalación casera y evita escribir en cada intento.
