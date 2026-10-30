package win.huansha.moneipe.gastos.aplicacion.servicio;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import win.huansha.moneipe.gastos.dominio.excepcion.RecursoNoEncontradoExcepcion;
import win.huansha.moneipe.gastos.dominio.modelo.AuditoriaGasto;
import win.huansha.moneipe.gastos.dominio.modelo.AuditoriaGasto;
import win.huansha.moneipe.gastos.dominio.modelo.ControlAsignacion;
import win.huansha.moneipe.gastos.dominio.modelo.ControlGasto;
import win.huansha.moneipe.gastos.dominio.modelo.GrupoMiembro;
import win.huansha.moneipe.gastos.dominio.puerto.entrada.ControlGastoCasoUso;
import win.huansha.moneipe.gastos.dominio.puerto.salida.AuditoriaGastoRepositorio;
import win.huansha.moneipe.gastos.dominio.puerto.salida.ControlAsignacionRepositorio;
import win.huansha.moneipe.gastos.dominio.puerto.salida.ControlGastoRepositorio;
import win.huansha.moneipe.gastos.dominio.puerto.salida.GastoRepositorio;
import win.huansha.moneipe.gastos.dominio.puerto.salida.GrupoMiembroRepositorio;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ControlGastoServicio implements ControlGastoCasoUso {

    private final ControlGastoRepositorio repo;
    private final GastoRepositorio gastoRepo;
    private final GrupoMiembroRepositorio grupoMiembroRepo;
    private final AuditoriaGastoRepositorio auditoriaRepo;
    private final ControlAsignacionRepositorio asignacionRepo;

    @Override
    public Mono<ControlGasto> crear(UUID creadoPor, String titulo, String descripcion, String tipo, String moneda) {
        return Mono.just(ControlGasto.builder()
            .id(UUID.randomUUID())
            .titulo(titulo)
            .descripcion(descripcion)
            .tipo(tipo)
            .moneda(moneda != null ? moneda : "PEN")
            .estadoControl("CREADO")
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

    public Flux<AuditoriaGasto> auditoria(UUID controlId) {
        return auditoriaRepo.porControl(controlId);
    }

    public Mono<ControlGasto> cambiarEstado(UUID id, String nuevoEstado) {
        return repo.porId(id)
            .switchIfEmpty(Mono.error(new RecursoNoEncontradoExcepcion("Control no encontrado: " + id)))
            .flatMap(existing -> {
                if ("COMPLETADO".equals(existing.getEstadoControl())) {
                    return Mono.error(new IllegalStateException("No se puede modificar un control completado"));
                }
                if (!"CREADO".equals(existing.getEstadoControl()) && "CREADO".equals(nuevoEstado)) {
                    return Mono.error(new IllegalStateException("No se puede volver a creado"));
                }
                if (!"CALCULADO".equals(existing.getEstadoControl()) && "COMPLETADO".equals(nuevoEstado)) {
                    return Mono.error(new IllegalStateException("Solo se puede completar desde calculado"));
                }
                ControlGasto actualizado = ControlGasto.builder()
                    .id(existing.getId())
                    .titulo(existing.getTitulo())
                    .descripcion(existing.getDescripcion())
                    .tipo(existing.getTipo())
                    .moneda(existing.getMoneda())
                    .estadoControl(nuevoEstado)
                    .asignaciones(null)
                    .grupoId(existing.getGrupoId())
                    .createdAt(existing.getCreatedAt())
                    .updatedAt(Instant.now())
                    .createdBy(existing.getCreatedBy())
                    .updatedBy(existing.getCreatedBy())
                    .build();
                return repo.guardar(actualizado);
            });
    }

    public Flux<ControlAsignacion> asignaciones(UUID controlId) {
        return asignacionRepo.porControl(controlId);
    }

    public Flux<ControlGasto> listarMios(UUID usuarioId, String tipo) {
        // Controles sin grupo creados por el usuario
        Flux<ControlGasto> sinGrupo = repo.porCreador(usuarioId)
            .filter(c -> c.getGrupoId() == null);
        // Controles vinculados a grupos donde el usuario es miembro
        Flux<ControlGasto> compartidos = grupoMiembroRepo.porUsuario(usuarioId)
            .map(GrupoMiembro::getGrupoId)
            .distinct()
            .flatMap(repo::porGrupo);
        Flux<ControlGasto> todos = Flux.concat(sinGrupo, compartidos)
            .distinct(ControlGasto::getId);
        if (tipo != null && !tipo.isBlank() && !"ALL".equalsIgnoreCase(tipo)) {
            todos = todos.filter(c -> tipo.equalsIgnoreCase(c.getTipo()));
        }
        return todos.flatMap(c -> gastoRepo.contarPorControl(c.getId())
            .map(cnt -> ControlGasto.builder()
                .id(c.getId())
                .titulo(c.getTitulo())
                .descripcion(c.getDescripcion())
                .tipo(c.getTipo())
                .moneda(c.getMoneda())
                .estadoControl(c.getEstadoControl())
                .asignaciones(c.getAsignaciones())
                .grupoId(c.getGrupoId())
                .cantidadGastos((int) cnt.longValue())
                .createdAt(c.getCreatedAt())
                .updatedAt(c.getUpdatedAt())
                .createdBy(c.getCreatedBy())
                .updatedBy(c.getUpdatedBy())
                .build()))
            .sort((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()));
    }

    @Override
    public Mono<ControlGasto> actualizar(UUID id, UUID actualizadoPor, String titulo, String descripcion, java.util.List<ControlAsignacion> asignaciones) {
        return repo.porId(id)
            .switchIfEmpty(Mono.error(new RecursoNoEncontradoExcepcion("Control no encontrado: " + id)))
            .flatMap(existing -> {
                ControlGasto actualizado = ControlGasto.builder()
                    .id(existing.getId())
                    .titulo(titulo != null ? titulo : existing.getTitulo())
                    .descripcion(descripcion != null ? descripcion : existing.getDescripcion())
                    .tipo(existing.getTipo())
                    .moneda(existing.getMoneda())
                    .estadoControl(existing.getEstadoControl())
                    .cantidadGastos(existing.getCantidadGastos())
                    .asignaciones(null)
                    .grupoId(existing.getGrupoId())
                    .createdAt(existing.getCreatedAt())
                    .updatedAt(Instant.now())
                    .createdBy(existing.getCreatedBy())
                    .updatedBy(actualizadoPor)
                    .build();
                return repo.guardar(actualizado)
                    .flatMap(saved -> {
                        if (asignaciones != null && !asignaciones.isEmpty()) {
                            List<ControlAsignacion> list = new ArrayList<>(asignaciones);
                            return asignacionRepo.guardarTodos(id, list)
                                .then(auditoriaRepo.guardar(AuditoriaGasto.builder()
                                    .id(UUID.randomUUID())
                                    .controlId(id)
                                    .usuarioId(actualizadoPor)
                                    .accion("ACTUALIZAR")
                                    .entidad("CONTROL")
                                    .entidadId(id)
                                    .valorAnterior(toJson(existing))
                                    .valorNuevo(toJson(saved))
                                    .build()))
                                .thenReturn(saved);
                        }
                        return auditoriaRepo.guardar(AuditoriaGasto.builder()
                            .id(UUID.randomUUID())
                            .controlId(id)
                            .usuarioId(actualizadoPor)
                            .accion("ACTUALIZAR")
                            .entidad("CONTROL")
                            .entidadId(id)
                            .valorAnterior(toJson(existing))
                            .valorNuevo(toJson(saved))
                            .build())
                            .thenReturn(saved);
                    });
            });
    }

    private String toJson(Object o) {
        return String.valueOf(o);
    }

    @Override
    public Mono<Void> eliminar(UUID id) {
        return repo.porId(id)
            .switchIfEmpty(Mono.error(new RecursoNoEncontradoExcepcion("Control no encontrado: " + id)))
            .flatMap(existing -> {
                if (!"CREADO".equals(existing.getEstadoControl())) {
                    return Mono.error(new IllegalStateException("Solo se puede eliminar en estado CREADO"));
                }
                return repo.eliminar(id);
            });
    }
}
