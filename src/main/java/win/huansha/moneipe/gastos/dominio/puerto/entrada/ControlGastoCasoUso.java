package win.huansha.moneipe.gastos.dominio.puerto.entrada;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import win.huansha.moneipe.gastos.dominio.modelo.ControlGasto;

import java.util.UUID;

public interface ControlGastoCasoUso {
    Mono<ControlGasto> crear(UUID creadoPor, String titulo, String descripcion);
    Mono<ControlGasto> porId(UUID id);
    Flux<ControlGasto> listarMios(UUID usuarioId);
    Mono<ControlGasto> actualizar(UUID id, UUID actualizadoPor, String titulo, String descripcion);
    Mono<Void> eliminar(UUID id);
}
