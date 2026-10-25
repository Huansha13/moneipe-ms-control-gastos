package win.huansha.moneipe.gastos.dominio.puerto.salida;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import win.huansha.moneipe.gastos.dominio.modelo.ControlGasto;

import java.util.UUID;

public interface ControlGastoRepositorio {
    Mono<ControlGasto> guardar(ControlGasto control);
    Mono<ControlGasto> porId(UUID id);
    Flux<ControlGasto> porCreador(UUID creadorId);
    Mono<Void> eliminar(UUID id);
}
