package win.huansha.moneipe.gastos.infraestructura.adaptador.salida.persistencia;

import lombok.RequiredArgsConstructor;

import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
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

    private final R2dbcEntityTemplate template;

    @Override
    public Mono<GrupoMiembro> guardar(GrupoMiembro miembro) {
        GrupoMiembroEntity e = new GrupoMiembroEntity();
        e.setGrupoId(miembro.getGrupoId());
        e.setUsuarioId(miembro.getUsuarioId());
        e.setRol(miembro.getRol());
        e.setFechaIngreso(miembro.getFechaIngreso() != null ? miembro.getFechaIngreso() : Instant.now());
        return template.insert(e)
            .map(saved -> GrupoMiembro.builder()
                .grupoId(saved.getGrupoId())
                .usuarioId(saved.getUsuarioId())
                .rol(saved.getRol())
                .fechaIngreso(saved.getFechaIngreso())
                .build());
    }

    @Override
    public Flux<GrupoMiembro> porGrupo(UUID grupoId) {
        return template.select(GrupoMiembroEntity.class)
            .all()
            .filter(e -> grupoId.equals(e.getGrupoId()))
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
            .any(e -> grupoId.equals(e.getGrupoId()) && usuarioId.equals(e.getUsuarioId()));
    }

    @Override
    public Flux<GrupoMiembro> porUsuario(UUID usuarioId) {
        return template.select(GrupoMiembroEntity.class)
            .all()
            .filter(e -> usuarioId.equals(e.getUsuarioId()))
            .map(e -> GrupoMiembro.builder()
                .grupoId(e.getGrupoId())
                .usuarioId(e.getUsuarioId())
                .rol(e.getRol())
                .fechaIngreso(e.getFechaIngreso())
                .build());
    }
}
