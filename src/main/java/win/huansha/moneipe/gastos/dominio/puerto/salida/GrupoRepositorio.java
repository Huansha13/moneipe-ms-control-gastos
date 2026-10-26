package win.huansha.moneipe.gastos.dominio.puerto.salida;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import win.huansha.moneipe.gastos.dominio.modelo.Grupo;

import java.util.UUID;

public interface GrupoRepositorio {
    Mono<Grupo> guardar(Grupo grupo);
    Mono<Grupo> porId(UUID id);
    Mono<Grupo> porControlId(UUID controlId);
    Mono<Void> eliminar(UUID id);
}
