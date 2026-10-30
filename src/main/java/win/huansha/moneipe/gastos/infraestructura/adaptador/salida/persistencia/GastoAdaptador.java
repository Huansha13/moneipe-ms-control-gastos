package win.huansha.moneipe.gastos.infraestructura.adaptador.salida.persistencia;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import win.huansha.moneipe.gastos.dominio.modelo.Gasto;
import win.huansha.moneipe.gastos.dominio.puerto.salida.GastoRepositorio;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class GastoAdaptador implements GastoRepositorio {

    private static final String ACTIVO = "ACTIVO";
    private static final String ELIMINADO = "ELIMINADO";

    private final GastoR2dbcRepository repo;

    @Override
    public Mono<Gasto> guardar(Gasto gasto) {
        GastoEntity e = new GastoEntity();
        e.setId(gasto.getId());
        e.setControlId(gasto.getControlId());
        e.setDescripcion(gasto.getDescripcion());
        e.setMonto(gasto.getMonto());
        e.setFecha(gasto.getFecha());
        e.setUsuarioId(gasto.getUsuarioId());
        e.setAsignaciones(gasto.getAsignaciones());
        e.setCreatedAt(gasto.getCreatedAt());
        e.setUpdatedAt(gasto.getUpdatedAt());
        e.setCreatedBy(gasto.getCreatedBy());
        e.setUpdatedBy(gasto.getUpdatedBy());
        if (e.getEstado() == null) e.setEstado(ACTIVO);
        return repo.save(e).map(this::toDomain);
    }

    @Override
    public Mono<Gasto> porId(UUID id) {
        return repo.findById(id)
            .filter(e -> ACTIVO.equals(e.getEstado()))
            .map(this::toDomain);
    }

    @Override
    public Mono<Long> contarPorControl(UUID controlId) {
        return repo.findAll()
            .filter(e -> controlId.equals(e.getControlId()) && "ACTIVO".equals(e.getEstado()))
            .count();
    }

    @Override
    public Flux<Gasto> porControl(UUID controlId) {
        return repo.findAll()
            .filter(e -> controlId.equals(e.getControlId()) && ACTIVO.equals(e.getEstado()))
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

    private Gasto toDomain(GastoEntity e) {
        return Gasto.builder()
            .id(e.getId())
            .controlId(e.getControlId())
            .descripcion(e.getDescripcion())
            .monto(e.getMonto())
            .fecha(e.getFecha())
            .usuarioId(e.getUsuarioId())
            .asignaciones(e.getAsignaciones())
            .createdAt(e.getCreatedAt())
            .updatedAt(e.getUpdatedAt())
            .createdBy(e.getCreatedBy())
            .updatedBy(e.getUpdatedBy())
            .build();
    }
}