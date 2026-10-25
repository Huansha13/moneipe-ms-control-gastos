-- V2: Eliminar tabla usuarios_proyeccion (proyección pasiva descartada).
-- La info de perfil vive en ms-usuarios; ms-control-gastos solo almacena
-- el UUID del usuario (sub del JWT) como foreign key sin restricción local.

-- Quitar FK de grupo_miembros hacia usuarios_proyeccion
ALTER TABLE grupo_miembros DROP CONSTRAINT IF EXISTS grupo_miembros_usuario_id_fkey;

-- Eliminar tabla de proyección
DROP TABLE IF EXISTS usuarios_proyeccion CASCADE;
