package win.huansha.moneipe.gastos.infraestructura.adaptador.salida.persistencia;

import lombok.RequiredArgsConstructor;

import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Component;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import win.huansha.moneipe.gastos.dominio.modelo.GrupoMiembro;
import win.huansha.moneipe.gastos.dominio.puerto.salida.GrupoMiembroRepositorio;

import java.time.Instant;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class GrupoMiembroAdaptador implements GrupoMiembroRepositorio {

    private static final String ACTIVO = "ACTIVO";
    private static final String ELIMINADO = "ELIMINADO";

    private final R2dbcEntityTemplate template;
    private final DatabaseClient databaseClient;

    @Override
    public Mono<GrupoMiembro> guardar(GrupoMiembro miembro) {
        Instant fecha = miembro.getFechaIngreso() != null ? miembro.getFechaIngreso() : Instant.now();
        return databaseClient.sql(
                "INSERT INTO grupo_miembros (grupo_id, usuario_id, rol, fecha_ingreso, estado) " +
                "VALUES (:grupoId, :usuarioId, :rol, :fecha, 'ACTIVO') " +
                "ON CONFLICT (grupo_id, usuario_id) DO UPDATE SET rol = :rol, estado = 'ACTIVO', deleted_at = NULL")
            .bind("grupoId", miembro.getGrupoId())
            .bind("usuarioId", miembro.getUsuarioId())
            .bind("rol", miembro.getRol())
            .bind("fecha", fecha)
            .fetch()
            .rowsUpdated()
            .then(Mono.just(GrupoMiembro.builder()
                .grupoId(miembro.getGrupoId())
                .usuarioId(miembro.getUsuarioId())
                .rol(miembro.getRol())
                .fechaIngreso(fecha)
                .build()));
    }

    @Override
    public Flux<GrupoMiembro> porGrupo(UUID grupoId) {
        return template.select(GrupoMiembroEntity.class)
            .all()
            .filter(e -> grupoId.equals(e.getGrupoId()) && ACTIVO.equals(e.getEstado()))
            .map(e -> GrupoMiembro.builder()
                .grupoId(e.getGrupoId())
                .usuarioId(e.getUsuarioId())
                .rol(e.getRol())
                .fechaIngreso(e.getFechaIngreso())
                .build());
    }

    @Override
    public Mono<Boolean> existe(UUID grupoId, UUID usuarioId) {
        return template.select(GrupoMiembroEntity.class)
            .all()
            .any(e -> grupoId.equals(e.getGrupoId()) && usuarioId.equals(e.getUsuarioId()) && ACTIVO.equals(e.getEstado()));
    }

    @Override
    public Mono<Void> eliminar(UUID grupoId, UUID usuarioId) {
        return databaseClient.sql("UPDATE grupo_miembros SET estado = 'ELIMINADO', deleted_at = NOW() WHERE grupo_id = :grupoId AND usuario_id = :usuarioId AND estado = 'ACTIVO'")
            .bind("grupoId", grupoId)
            .bind("usuarioId", usuarioId)
            .fetch()
            .rowsUpdated()
            .then();
    }

    @Override
    public Mono<Void> actualizarRol(UUID grupoId, UUID usuarioId, String rol) {
        return databaseClient.sql("UPDATE grupo_miembros SET rol = :rol WHERE grupo_id = :grupoId AND usuario_id = :usuarioId AND estado = 'ACTIVO'")
            .bind("rol", rol)
            .bind("grupoId", grupoId)
            .bind("usuarioId", usuarioId)
            .fetch()
            .rowsUpdated()
            .then();
    }

    @Override
    public Flux<GrupoMiembro> porUsuario(UUID usuarioId) {
        return template.select(GrupoMiembroEntity.class)
            .all()
            .filter(e -> usuarioId.equals(e.getUsuarioId()) && ACTIVO.equals(e.getEstado()))
            .map(e -> GrupoMiembro.builder()
                .grupoId(e.getGrupoId())
                .usuarioId(e.getUsuarioId())
                .rol(e.getRol())
                .fechaIngreso(e.getFechaIngreso())
                .build());
    }
}
