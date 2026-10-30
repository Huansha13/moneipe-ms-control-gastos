CREATE TABLE IF NOT EXISTS control_asignaciones (
    control_id  UUID NOT NULL REFERENCES controles_gasto (id),
    usuario_id  UUID NOT NULL,
    porcentaje  DECIMAL(5,2) NOT NULL,
    PRIMARY KEY (control_id, usuario_id)
);

CREATE INDEX IF NOT EXISTS idx_control_asignaciones_control ON control_asignaciones (control_id);