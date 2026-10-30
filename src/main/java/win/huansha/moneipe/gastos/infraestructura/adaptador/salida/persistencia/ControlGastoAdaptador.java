package win.huansha.moneipe.gastos.infraestructura.adaptador.salida.persistencia;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import win.huansha.moneipe.gastos.dominio.modelo.ControlGasto;
import win.huansha.moneipe.gastos.dominio.puerto.salida.ControlGastoRepositorio;

import java.time.Instant;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ControlGastoAdaptador implements ControlGastoRepositorio {

    private static final String ACTIVO = "ACTIVO";
    private static final String ELIMINADO = "ELIMINADO";

    private final ControlGastoR2dbcRepository repo;

    @Override
    public Mono<ControlGasto> guardar(ControlGasto control) {
        ControlGastoEntity e = new ControlGastoEntity();
        e.setId(control.getId());
        e.setTitulo(control.getTitulo());
        e.setDescripcion(control.getDescripcion());
        e.setTipo(control.getTipo());
        e.setMoneda(control.getMoneda() != null ? control.getMoneda() : "PEN");
        e.setEstadoControl(control.getEstadoControl() != null ? control.getEstadoControl() : "CREADO");
        e.setAsignaciones(control.getAsignaciones());
        e.setGrupoId(control.getGrupoId());
        e.setCreatedAt(control.getCreatedAt());
        e.setUpdatedAt(control.getUpdatedAt());
        e.setCreatedBy(control.getCreatedBy());
        e.setUpdatedBy(control.getUpdatedBy());
        if (e.getEstado() == null) {
            e.setEstado(ACTIVO);
        }
        return repo.save(e).map(this::toDomain);
    }

    @Override
    public Mono<ControlGasto> porId(UUID id) {
        return repo.findById(id)
            .filter(e -> ACTIVO.equals(e.getEstado()))
            .map(this::toDomain);
    }

    @Override
    public Flux<ControlGasto> porCreador(UUID creadorId) {
        return repo.findAll()
            .filter(e -> creadorId.equals(e.getCreatedBy()) && ACTIVO.equals(e.getEstado()))
            .map(this::toDomain);
    }

    @Override
    public Flux<ControlGasto> porGrupo(UUID grupoId) {
        return repo.findAll()
            .filter(e -> grupoId.equals(e.getGrupoId()) && ACTIVO.equals(e.getEstado()))
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

    private ControlGasto toDomain(ControlGastoEntity e) {
        return ControlGasto.builder()
            .id(e.getId())
            .titulo(e.getTitulo())
            .descripcion(e.getDescripcion())
            .tipo(e.getTipo())
            .moneda(e.getMoneda())
            .estadoControl(e.getEstadoControl())
            .asignaciones(e.getAsignaciones())
            .grupoId(e.getGrupoId())
            .createdAt(e.getCreatedAt())
            .updatedAt(e.getUpdatedAt())
            .createdBy(e.getCreatedBy())
            .updatedBy(e.getUpdatedBy())
            .build();
    }
}
