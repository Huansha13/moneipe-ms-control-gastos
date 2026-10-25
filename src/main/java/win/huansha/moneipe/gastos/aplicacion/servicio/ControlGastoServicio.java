package win.huansha.moneipe.gastos.aplicacion.servicio;

import lombok.RequiredArgsConstructor;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import win.huansha.moneipe.gastos.dominio.excepcion.RecursoNoEncontradoExcepcion;
import win.huansha.moneipe.gastos.dominio.modelo.ControlGasto;
import win.huansha.moneipe.gastos.dominio.puerto.entrada.ControlGastoCasoUso;
import win.huansha.moneipe.gastos.dominio.puerto.salida.ControlGastoRepositorio;

import java.time.Instant;
import java.util.UUID;

@org.springframework.stereotype.Service
@RequiredArgsConstructor
public class ControlGastoServicio implements ControlGastoCasoUso {

    private final ControlGastoRepositorio repo;

    @Override
    public Mono<ControlGasto> crear(UUID creadoPor, String titulo, String descripcion) {
        return Mono.just(ControlGasto.builder()
            .id(UUID.randomUUID())
            .titulo(titulo)
            .descripcion(descripcion)
            .grupoId(null)
            .createdBy(creadoPor)
            .updatedBy(creadoPor)
            .build())
            .flatMap(repo::guardar);
    }

    @Override
    public Mono<ControlGasto> porId(UUID id) {
        return repo.porId(id)
            .switchIfEmpty(Mono.error(new RecursoNoEncontradoExcepcion("Control no encontrado: " + id)));
    }

    @Override
    public Flux<ControlGasto> listarMios(UUID usuarioId) {
        return repo.porCreador(usuarioId);
    }

    @Override
    public Mono<ControlGasto> actualizar(UUID id, UUID actualizadoPor, String titulo, String descripcion) {
        return repo.porId(id)
            .switchIfEmpty(Mono.error(new RecursoNoEncontradoExcepcion("Control no encontrado: " + id)))
            .map(existing -> ControlGasto.builder()
                .id(existing.getId())
                .titulo(titulo != null ? titulo : existing.getTitulo())
                .descripcion(descripcion != null ? descripcion : existing.getDescripcion())
                .grupoId(existing.getGrupoId())
                .createdAt(existing.getCreatedAt())
                .updatedAt(Instant.now())
                .createdBy(existing.getCreatedBy())
                .updatedBy(actualizadoPor)
                .build())
            .flatMap(repo::guardar);
    }

    @Override
    public Mono<Void> eliminar(UUID id) {
        return repo.porId(id)
            .switchIfEmpty(Mono.error(new RecursoNoEncontradoExcepcion("Control no encontrado: " + id)))
            .then(repo.eliminar(id));
    }
}
