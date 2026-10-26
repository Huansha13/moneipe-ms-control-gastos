package win.huansha.moneipe.gastos.infraestructura.adaptador.salida.persistencia;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

import reactor.core.publisher.Mono;

import win.huansha.moneipe.gastos.dominio.modelo.Grupo;
import win.huansha.moneipe.gastos.dominio.puerto.salida.GrupoRepositorio;

import java.time.Instant;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class GrupoAdaptador implements GrupoRepositorio {

    private static final String ACTIVO = "ACTIVO";
    private static final String ELIMINADO = "ELIMINADO";

    private final GrupoR2dbcRepository repo;

    @Override
    public Mono<Grupo> guardar(Grupo grupo) {
        GrupoEntity e = new GrupoEntity();
        e.setId(grupo.getId());
        e.setNombre(grupo.getNombre());
        e.setDescripcion(grupo.getDescripcion());
        e.setControlId(grupo.getControlId());
        e.setCreatedBy(grupo.getCreatedBy());
        e.setUpdatedBy(grupo.getUpdatedBy());
        if (e.getEstado() == null) {
            e.setEstado(ACTIVO);
        }
        return repo.save(e).map(this::toDomain);
    }

    @Override
    public Mono<Grupo> porId(UUID id) {
        return repo.findById(id)
            .filter(e -> ACTIVO.equals(e.getEstado()))
            .map(this::toDomain);
    }

    @Override
    public Mono<Grupo> porControlId(UUID controlId) {
        return repo.findAll()
            .filter(e -> controlId.equals(e.getControlId()) && ACTIVO.equals(e.getEstado()))
            .next()
            .map(this::toDomain);
    }

    @Override
    public Mono<Void> eliminar(UUID id) {
        return repo.findById(id)
            .filter(e -> ACTIVO.equals(e.getEstado()))
            .flatMap(e -> {
                e.setEstado(ELIMINADO);
                e.setDeletedAt(Instant.now());
                e.setUpdatedAt(Instant.now());
                return repo.save(e);
            })
            .then();
    }

    private Grupo toDomain(GrupoEntity e) {
        return Grupo.builder()
            .id(e.getId()).nombre(e.getNombre()).descripcion(e.getDescripcion())
            .controlId(e.getControlId()).createdAt(e.getCreatedAt())
            .updatedAt(e.getUpdatedAt()).createdBy(e.getCreatedBy())
            .updatedBy(e.getUpdatedBy()).build();
    }
}
