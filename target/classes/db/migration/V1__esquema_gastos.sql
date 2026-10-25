CREATE TABLE usuarios_proyeccion (
    id         UUID PRIMARY KEY,
    username   VARCHAR(50)  NOT NULL,
    nombres    VARCHAR(100),
    apellidos  VARCHAR(100)
);

CREATE TABLE grupos (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nombre      VARCHAR(100) NOT NULL,
    descripcion TEXT,
    control_id  UUID UNIQUE,
    created_at  TIMESTAMP,
    updated_at  TIMESTAMP,
    created_by  UUID,
    updated_by  UUID
);

CREATE TABLE controles_gasto (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    titulo      VARCHAR(150) NOT NULL,
    descripcion TEXT,
    grupo_id    UUID REFERENCES grupos (id),
    created_at  TIMESTAMP,
    updated_at  TIMESTAMP,
    created_by  UUID,
    updated_by  UUID
);

ALTER TABLE grupos
    ADD CONSTRAINT fk_grupos_control FOREIGN KEY (control_id) REFERENCES controles_gasto (id);

CREATE TABLE grupo_miembros (
    grupo_id      UUID NOT NULL REFERENCES grupos (id),
    usuario_id    UUID NOT NULL REFERENCES usuarios_proyeccion (id),
    rol           VARCHAR(20),
    fecha_ingreso TIMESTAMP NOT NULL DEFAULT NOW(),
    PRIMARY KEY (grupo_id, usuario_id)
);

CREATE INDEX idx_controles_gasto_grupo ON controles_gasto (grupo_id);
CREATE INDEX idx_grupo_miembros_usuario ON grupo_miembros (usuario_id);
