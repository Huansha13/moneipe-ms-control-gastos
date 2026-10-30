package win.huansha.moneipe.gastos.aplicacion.servicio;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import win.huansha.moneipe.gastos.dominio.excepcion.RecursoNoEncontradoExcepcion;
import win.huansha.moneipe.gastos.dominio.modelo.ControlGasto;
import win.huansha.moneipe.gastos.dominio.modelo.Grupo;
import win.huansha.moneipe.gastos.dominio.modelo.GrupoMiembro;
import win.huansha.moneipe.gastos.dominio.puerto.entrada.GrupoCasoUso;
import win.huansha.moneipe.gastos.dominio.puerto.salida.ControlGastoRepositorio;
import win.huansha.moneipe.gastos.dominio.puerto.salida.GrupoMiembroRepositorio;
import win.huansha.moneipe.gastos.dominio.puerto.salida.GrupoRepositorio;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GrupoServicio implements GrupoCasoUso {

    private final ControlGastoRepositorio controlRepo;
    private final GrupoRepositorio grupoRepo;
    private final GrupoMiembroRepositorio miembroRepo;

    @Value("${ms.usuarios.base-url:http://localhost:8084}")
    private String usuariosBaseUrl;

    @Override
    @Transactional
    public Mono<Grupo> crear(UUID creadorId, String titulo, String descripcion, List<UUID> miembrosIds) {
        // El grupo se crea de forma independiente; el control de gasto se vincula después
        // (via POST /api/gastos/controles/{id}/invitar) si se desea asociar uno.
        Grupo grupo = Grupo.builder()
            .id(UUID.randomUUID())
            .nombre(titulo)
            .descripcion(descripcion)
            .controlId(null)
            .createdBy(creadorId)
            .updatedBy(creadorId)
            .build();

        return grupoRepo.guardar(grupo)
            .flatMap(savedGrupo -> agregarMiembros(savedGrupo.getId(), creadorId, miembrosIds)
                .thenReturn(savedGrupo));
    }

    @Override
    @Transactional
    public Mono<Grupo> invitar(UUID controlId, UUID usuarioId, String rol) {
        String rolFinal = (rol != null && !rol.isBlank()) ? rol : "MEMBER";

        return controlRepo.porId(controlId)
            .switchIfEmpty(Mono.error(new RecursoNoEncontradoExcepcion("Control no encontrado: " + controlId)))
            .flatMap(control -> {
                if (!"CREADO".equals(control.getEstadoControl())) {
                    return Mono.error(new IllegalStateException("No se pueden modificar miembros fuera de estado CREADO"));
                }
                if (control.getGrupoId() != null) {
                    return grupoRepo.porId(control.getGrupoId())
                        .flatMap(grupo -> agregarMiembro(grupo, usuarioId, rolFinal)
                            .thenReturn(grupo));
                }
                Grupo grupo = Grupo.builder()
                    .id(UUID.randomUUID())
                    .nombre(control.getTitulo())
                    .descripcion(control.getDescripcion())
                    .controlId(control.getId())
                    .createdBy(control.getCreatedBy())
                    .updatedBy(control.getCreatedBy())
                    .build();
                return grupoRepo.guardar(grupo)
                    .flatMap(grupoGuardado -> {
                        ControlGasto controlActualizado = ControlGasto.builder()
                            .id(control.getId())
                            .titulo(control.getTitulo())
                            .descripcion(control.getDescripcion())
                            .tipo(control.getTipo())
                            .grupoId(grupoGuardado.getId())
                            .createdAt(control.getCreatedAt())
                            .updatedAt(Instant.now())
                            .createdBy(control.getCreatedBy())
                            .updatedBy(control.getCreatedBy())
                            .build();
                        Mono<Void> agregar = agregarMiembro(grupoGuardado, control.getCreatedBy(), "ADMIN");
                        if (!usuarioId.equals(control.getCreatedBy())) {
                            agregar = agregar.then(agregarMiembro(grupoGuardado, usuarioId, rolFinal));
                        }
                        return controlRepo.guardar(controlActualizado)
                            .then(agregar)
                            .thenReturn(grupoGuardado);
                    });
            });
    }

    private Mono<Void> agregarMiembros(UUID grupoId, UUID creadorId, List<UUID> miembrosIds) {
        Mono<Void> creador = agregarMiembroPorId(grupoId, creadorId, "ADMIN");
        Mono<Void> miembros = Flux.fromIterable(miembrosIds)
            .filter(uid -> !uid.equals(creadorId))
            .flatMap(uid -> agregarMiembroPorId(grupoId, uid, "MEMBER"))
            .then();
        return creador.then(miembros);
    }

    private Mono<Void> agregarMiembro(Grupo grupo, UUID usuarioId, String rol) {
        return agregarMiembroPorId(grupo.getId(), usuarioId, rol);
    }

    private Mono<Void> agregarMiembroPorId(UUID grupoId, UUID usuarioId, String rol) {
        return miembroRepo.existe(grupoId, usuarioId)
            .flatMap(yaExiste -> {
                if (yaExiste) {
                    // Ya es miembro: actualizar su rol si es diferente
                    return miembroRepo.porGrupo(grupoId)
                        .filter(m -> m.getUsuarioId().equals(usuarioId))
                        .next()
                        .flatMap(existing -> {
                            if (rol.equals(existing.getRol())) return Mono.empty();
                            // Actualizar rol en BD
                            return miembroRepo.actualizarRol(grupoId, usuarioId, rol);
                        });
                }
                GrupoMiembro miembro = GrupoMiembro.builder()
                    .grupoId(grupoId)
                    .usuarioId(usuarioId)
                    .rol(rol)
                    .fechaIngreso(Instant.now())
                    .build();
                return miembroRepo.guardar(miembro).then();
            });
    }

    @Override
    public Mono<Grupo> porControlId(UUID controlId) {
        return grupoRepo.porControlId(controlId);
    }

    @Override
    public Flux<GrupoMiembro> miembros(UUID grupoId) {
        return miembroRepo.porGrupo(grupoId);
    }

    @Override
    public Flux<java.util.Map<String, Object>> miembrosUsuarios(UUID grupoId) {
        return miembroRepo.porGrupo(grupoId)
            .map(GrupoMiembro::getUsuarioId)
            .distinct()
            .flatMap(uid -> Mono.fromCallable(() -> {
                java.net.http.HttpClient client = java.net.http.HttpClient.newBuilder()
                    .proxy(java.net.ProxySelector.of(null)).build();
                java.net.http.HttpRequest req = java.net.http.HttpRequest.newBuilder(
                    java.net.URI.create(usuariosBaseUrl + "/api/usuarios/" + uid)).GET().build();
                java.net.http.HttpResponse<String> resp = client.send(req, java.net.http.HttpResponse.BodyHandlers.ofString());
                String body = resp.body();
                java.util.Map<String, Object> m = new java.util.HashMap<>();
                m.put("id", uid.toString());
                m.put("nombres", extractField(body, "nombres"));
                m.put("apellidos", extractField(body, "apellidos"));
                m.put("email", extractField(body, "email"));
                m.put("username", extractField(body, "username"));
                return m;
            }).subscribeOn(reactor.core.scheduler.Schedulers.boundedElastic()));
    }

    private String extractField(String json, String field) {
        if (json == null) return "";
        int idx = json.indexOf("\"" + field + "\"");
        if (idx < 0) return "";
        int colon = json.indexOf(":", idx);
        if (colon < 0) return "";
        String rest = json.substring(colon + 1).trim();
        if (rest.startsWith("\"")) {
            int end = rest.indexOf("\"", 1);
            return end > 0 ? rest.substring(1, end) : "";
        }
        int end = rest.indexOf(",");
        if (end < 0) end = rest.indexOf("}");
        return end > 0 ? rest.substring(0, end).trim() : rest.trim();
    }

    @Override
    public Flux<Grupo> listarMios(UUID usuarioId) {
        return miembroRepo.porUsuario(usuarioId)
            .map(GrupoMiembro::getGrupoId)
            .distinct()
            .flatMap(grupoRepo::porId)
            .sort((a, b) -> {
                if (a.getCreatedAt() == null) return 1;
                if (b.getCreatedAt() == null) return -1;
                return b.getCreatedAt().compareTo(a.getCreatedAt());
            });
    }

    @Override
    public Mono<Void> agregarMiembro(UUID grupoId, UUID usuarioId, String rol) {
        String rolFinal = (rol != null && !rol.isBlank()) ? rol : "MEMBER";
        return agregarMiembroPorId(grupoId, usuarioId, rolFinal);
    }

    @Override
    public Mono<Void> eliminarMiembro(UUID grupoId, UUID usuarioId) {
        return miembroRepo.eliminar(grupoId, usuarioId);
    }

    @Override
    public Mono<Void> salir(UUID grupoId, UUID usuarioId) {
        return grupoRepo.porId(grupoId)
            .switchIfEmpty(Mono.error(new RecursoNoEncontradoExcepcion("Grupo no encontrado: " + grupoId)))
            .flatMap(grupo -> miembroRepo.porGrupo(grupoId).collectList()
                .flatMap(miembros -> {
                    boolean eraAdmin = miembros.stream()
                        .anyMatch(m -> m.getUsuarioId().equals(usuarioId) && "ADMIN".equals(m.getRol()));
                    List<GrupoMiembro> restantes = miembros.stream()
                        .filter(m -> !m.getUsuarioId().equals(usuarioId))
                        .toList();

                    // Eliminar al usuario
                    Mono<Void> eliminar = miembroRepo.eliminar(grupoId, usuarioId);

                    if (restantes.isEmpty()) {
                        // Último miembro: eliminar grupo y desvincular control
                        return eliminar
                            .then(controlRepo.porId(grupo.getControlId())
                                .flatMap(control -> {
                                    ControlGasto controlActualizado = ControlGasto.builder()
                                        .id(control.getId())
                                        .titulo(control.getTitulo())
                                        .descripcion(control.getDescripcion())
                                        .grupoId(null)
                                        .createdAt(control.getCreatedAt())
                                        .updatedAt(Instant.now())
                                        .createdBy(control.getCreatedBy())
                                        .updatedBy(control.getCreatedBy())
                                        .build();
                                    return controlRepo.guardar(controlActualizado);
                                })
                                .then(grupoRepo.eliminar(grupoId)))
                            .then();
                    }

                    // Si era admin, elegir nuevo admin aleatorio
                    if (eraAdmin && !restantes.isEmpty()) {
                        GrupoMiembro nuevoAdmin = restantes.get(
                            (int) (Math.random() * restantes.size()));
                        return eliminar
                            .then(miembroRepo.actualizarRol(grupoId, nuevoAdmin.getUsuarioId(), "ADMIN"));
                    }

                    return eliminar;
                }));
    }
}
