package win.huansha.moneipe.gastos.infraestructura.adaptador.salida.persistencia;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import win.huansha.moneipe.gastos.dominio.modelo.ControlGasto;
import win.huansha.moneipe.gastos.dominio.puerto.salida.ControlGastoRepositorio;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ControlGastoAdaptador implements ControlGastoRepositorio {

    private final ControlGastoR2dbcRepository repo;

    @Override
    public Mono<ControlGasto> guardar(ControlGasto control) {
        ControlGastoEntity e = new ControlGastoEntity();
        e.setId(control.getId());
        e.setTitulo(control.getTitulo());
        e.setDescripcion(control.getDescripcion());
        e.setGrupoId(control.getGrupoId());
        e.setCreatedAt(control.getCreatedAt());
        e.setUpdatedAt(control.getUpdatedAt());
        e.setCreatedBy(control.getCreatedBy());
        e.setUpdatedBy(control.getUpdatedBy());
        return repo.save(e).map(this::toDomain);
    }

    @Override
    public Mono<ControlGasto> porId(UUID id) {
        return repo.findById(id).map(this::toDomain);
    }

    @Override
    public Flux<ControlGasto> porCreador(UUID creadorId) {
        return repo.findAll()
            .filter(e -> creadorId.equals(e.getCreatedBy()))
            .map(this::toDomain);
    }

    @Override
    public Mono<Void> eliminar(UUID id) {
        return repo.deleteById(id);
    }

    private ControlGasto toDomain(ControlGastoEntity e) {
        return ControlGasto.builder()
            .id(e.getId())
            .titulo(e.getTitulo())
            .descripcion(e.getDescripcion())
            .grupoId(e.getGrupoId())
            .createdAt(e.getCreatedAt())
            .updatedAt(e.getUpdatedAt())
            .createdBy(e.getCreatedBy())
            .updatedBy(e.getUpdatedBy())
            .build();
    }
}
