package win.huansha.moneipe.gastos.dominio.puerto.salida;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import win.huansha.moneipe.gastos.dominio.modelo.GrupoMiembro;

import java.util.UUID;

public interface GrupoMiembroRepositorio {
    Mono<GrupoMiembro> guardar(GrupoMiembro miembro);
    Flux<GrupoMiembro> porGrupo(UUID grupoId);
    Mono<Boolean> existe(UUID grupoId, UUID usuarioId);
    Flux<GrupoMiembro> porUsuario(UUID usuarioId);
    Mono<Void> eliminar(UUID grupoId, UUID usuarioId);
    Mono<Void> actualizarRol(UUID grupoId, UUID usuarioId, String rol);
}
