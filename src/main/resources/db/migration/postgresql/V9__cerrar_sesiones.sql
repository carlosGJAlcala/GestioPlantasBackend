-- S9 (entrega 2026-11-17): «Cerrar sesión en todos los equipos». Los JWT emitidos antes de este momento dejan de
-- valer, igual que con contrasena_cambiada (V8), pero sin cambiar la contraseña: p. ej. tras usar un ordenador ajeno
-- o perder el móvil. La pone la propia persona (POST /persona/sesiones/cerrar) o el admin (POST /persona/{id}/...).
ALTER TABLE persona ADD COLUMN sesiones_cerradas TIMESTAMP;
