package win.huansha.moneipe.gastos.dominio.puerto.salida;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import win.huansha.moneipe.gastos.dominio.modelo.AuditoriaGasto;
import java.util.UUID;

public interface AuditoriaGastoRepositorio {
    Mono<AuditoriaGasto> guardar(AuditoriaGasto a);
    Flux<AuditoriaGasto> porControl(UUID controlId);
}