package win.huansha.moneipe.gastos.infraestructura.adaptador.salida.persistencia;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import win.huansha.moneipe.gastos.dominio.modelo.AuditoriaGasto;
import win.huansha.moneipe.gastos.dominio.puerto.salida.AuditoriaGastoRepositorio;
import java.time.Instant;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AuditoriaGastoAdaptador implements AuditoriaGastoRepositorio {

    private final AuditoriaGastoR2dbcRepository repo;

    @Override
    public Mono<AuditoriaGasto> guardar(AuditoriaGasto a) {
        AuditoriaGastoEntity e = new AuditoriaGastoEntity();
        e.setId(a.getId());
        e.setControlId(a.getControlId());
        e.setUsuarioId(a.getUsuarioId());
        e.setAccion(a.getAccion());
        e.setEntidad(a.getEntidad());
        e.setEntidadId(a.getEntidadId());
        e.setValorAnterior(a.getValorAnterior());
        e.setValorNuevo(a.getValorNuevo());
        e.setFecha(a.getFecha() != null ? a.getFecha() : Instant.now());
        return repo.save(e).map(this::toDomain);
    }

    @Override
    public Flux<AuditoriaGasto> porControl(UUID controlId) {
        return repo.findAll()
            .filter(e -> controlId.equals(e.getControlId()))
            .sort((a, b) -> b.getFecha().compareTo(a.getFecha()))
            .map(this::toDomain);
    }

    private AuditoriaGasto toDomain(AuditoriaGastoEntity e) {
        return AuditoriaGasto.builder()
            .id(e.getId())
            .controlId(e.getControlId())
            .usuarioId(e.getUsuarioId())
            .accion(e.getAccion())
            .entidad(e.getEntidad())
            .entidadId(e.getEntidadId())
            .valorAnterior(e.getValorAnterior())
            .valorNuevo(e.getValorNuevo())
            .fecha(e.getFecha())
            .build();
    }
}