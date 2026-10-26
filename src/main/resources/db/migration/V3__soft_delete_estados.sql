-- Soft delete: agregar columna estado a todas las tablas
ALTER TABLE controles_gasto ADD COLUMN estado VARCHAR(20) NOT NULL DEFAULT 'ACTIVO';
ALTER TABLE grupos ADD COLUMN estado VARCHAR(20) NOT NULL DEFAULT 'ACTIVO';
ALTER TABLE grupo_miembros ADD COLUMN estado VARCHAR(20) NOT NULL DEFAULT 'ACTIVO';

-- Timestamp de eliminacion para auditoria
ALTER TABLE controles_gasto ADD COLUMN deleted_at TIMESTAMP;
ALTER TABLE grupos ADD COLUMN deleted_at TIMESTAMP;
ALTER TABLE grupo_miembros ADD COLUMN deleted_at TIMESTAMP;
