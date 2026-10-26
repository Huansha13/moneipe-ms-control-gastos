package win.huansha.moneipe.gastos.dominio.puerto.entrada;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import win.huansha.moneipe.gastos.dominio.modelo.Grupo;
import win.huansha.moneipe.gastos.dominio.modelo.GrupoMiembro;

import java.util.List;
import java.util.UUID;

public interface GrupoCasoUso {
    Mono<Grupo> crear(UUID creadorId, String titulo, String descripcion, List<UUID> miembrosIds);
    Mono<Grupo> porControlId(UUID controlId);
    Flux<Grupo> listarMios(UUID usuarioId);
    Flux<GrupoMiembro> miembros(UUID grupoId);
    Mono<Grupo> invitar(UUID controlId, UUID usuarioId, String rol);
    Mono<Void> salir(UUID grupoId, UUID usuarioId);
}
