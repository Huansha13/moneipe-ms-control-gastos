CREATE TABLE auditoria_gastos (
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    control_id     UUID NOT NULL,
    usuario_id     UUID,
    accion         VARCHAR(50) NOT NULL,
    entidad        VARCHAR(50) NOT NULL,
    entidad_id     UUID,
    valor_anterior TEXT,
    valor_nuevo    TEXT,
    fecha          TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_auditoria_control ON auditoria_gastos (control_id);
CREATE INDEX idx_auditoria_fecha ON auditoria_gastos (fecha);