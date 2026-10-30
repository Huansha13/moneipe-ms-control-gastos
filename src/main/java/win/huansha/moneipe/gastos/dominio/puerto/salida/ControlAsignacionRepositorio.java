package win.huansha.moneipe.gastos.dominio.puerto.salida;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import win.huansha.moneipe.gastos.dominio.modelo.ControlAsignacion;
import java.util.UUID;

public interface ControlAsignacionRepositorio {
    Flux<ControlAsignacion> guardarTodos(UUID controlId, java.util.List<ControlAsignacion> asignaciones);
    Flux<ControlAsignacion> porControl(UUID controlId);
    Flux<ControlAsignacion> porControlTodos(UUID controlId);
    Mono<Void> eliminarPorControl(UUID controlId);
}