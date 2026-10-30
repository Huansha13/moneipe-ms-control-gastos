package win.huansha.moneipe.gastos.dominio.puerto.salida;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import win.huansha.moneipe.gastos.dominio.modelo.Gasto;
import java.util.UUID;

public interface GastoRepositorio {
    Mono<Gasto> guardar(Gasto gasto);
    Mono<Gasto> porId(UUID id);
    Flux<Gasto> porControl(UUID controlId);
    Mono<Long> contarPorControl(UUID controlId);
    Mono<Void> eliminar(UUID id);
}