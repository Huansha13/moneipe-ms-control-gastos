CREATE TABLE gastos (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    control_id  UUID NOT NULL REFERENCES controles_gasto (id),
    descripcion TEXT,
    monto       DECIMAL(18,2) NOT NULL,
    fecha       DATE NOT NULL,
    created_at  TIMESTAMP,
    updated_at  TIMESTAMP,
    created_by  UUID,
    updated_by  UUID,
    estado      VARCHAR(20) DEFAULT 'ACTIVO',
    deleted_at  TIMESTAMP
);

CREATE INDEX idx_gastos_control ON gastos (control_id);
CREATE INDEX idx_gastos_fecha ON gastos (fecha);