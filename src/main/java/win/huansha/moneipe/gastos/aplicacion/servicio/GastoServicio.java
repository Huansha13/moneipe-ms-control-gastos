package win.huansha.moneipe.gastos.aplicacion.servicio;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import win.huansha.moneipe.gastos.dominio.excepcion.RecursoNoEncontradoExcepcion;
import win.huansha.moneipe.gastos.dominio.modelo.ControlAsignacion;
import win.huansha.moneipe.gastos.dominio.modelo.ControlGasto;
import win.huansha.moneipe.gastos.dominio.modelo.Gasto;
import win.huansha.moneipe.gastos.dominio.modelo.GrupoMiembro;
import win.huansha.moneipe.gastos.dominio.puerto.entrada.GastoCasoUso;
import win.huansha.moneipe.gastos.dominio.puerto.salida.ControlAsignacionRepositorio;
import win.huansha.moneipe.gastos.dominio.puerto.salida.ControlGastoRepositorio;
import win.huansha.moneipe.gastos.dominio.puerto.salida.GastoRepositorio;
import win.huansha.moneipe.gastos.dominio.puerto.salida.GrupoMiembroRepositorio;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GastoServicio implements GastoCasoUso {

    private final GastoRepositorio repo;
    private final ControlGastoRepositorio controlRepo;
    private final GrupoMiembroRepositorio grupoMiembroRepo;
    private final ControlAsignacionRepositorio asignacionRepo;

    @Value("${ms.usuarios.base-url:http://localhost:8084}")
    private String usuariosBaseUrl;

    @Override
    public Mono<Gasto> crear(UUID controlId, UUID creadoPor, String descripcion, BigDecimal monto, LocalDate fecha, UUID usuarioId, String asignaciones) {
        return controlRepo.porId(controlId)
            .switchIfEmpty(Mono.error(new RecursoNoEncontradoExcepcion("Control no encontrado: " + controlId)))
            .flatMap(control -> {
                if ("COMPLETADO".equals(control.getEstadoControl())) {
                    return Mono.error(new IllegalStateException("No se puede modificar un control completado"));
                }
                return Mono.just(control);
            })
            .then(Mono.fromCallable(() -> Gasto.builder()
                .id(UUID.randomUUID())
                .controlId(controlId)
                .descripcion(descripcion)
                .monto(monto)
                .fecha(fecha != null ? fecha : LocalDate.now())
                .usuarioId(usuarioId)
                .asignaciones(asignaciones)
                .createdBy(creadoPor)
                .updatedBy(creadoPor)
                .build()))
            .flatMap(repo::guardar);
    }

    @Override
    public Flux<Gasto> listarPorControl(UUID controlId) {
        return repo.porControl(controlId)
            .sort((a, b) -> {
                int cmp = b.getFecha().compareTo(a.getFecha());
                if (cmp != 0) return cmp;
                if (a.getCreatedAt() == null) return 1;
                if (b.getCreatedAt() == null) return -1;
                return b.getCreatedAt().compareTo(a.getCreatedAt());
            });
    }

    @Override
    public Mono<Gasto> actualizar(UUID id, String descripcion, BigDecimal monto, LocalDate fecha, UUID usuarioId, String asignaciones) {
        return repo.porId(id)
            .switchIfEmpty(Mono.error(new RecursoNoEncontradoExcepcion("Gasto no encontrado: " + id)))
            .flatMap(existing -> controlRepo.porId(existing.getControlId())
                .switchIfEmpty(Mono.error(new RecursoNoEncontradoExcepcion("Control no encontrado")))
                .flatMap(control -> {
                    if ("COMPLETADO".equals(control.getEstadoControl())) {
                        return Mono.error(new IllegalStateException("No se puede modificar un control completado"));
                    }
                    return Mono.just(existing);
                }))
            .map(existing -> Gasto.builder()
                .id(existing.getId())
                .controlId(existing.getControlId())
                .descripcion(descripcion != null ? descripcion : existing.getDescripcion())
                .monto(monto != null ? monto : existing.getMonto())
                .fecha(fecha != null ? fecha : existing.getFecha())
                .usuarioId(usuarioId)
                .asignaciones(asignaciones)
                .createdAt(existing.getCreatedAt())
                .updatedAt(Instant.now())
                .createdBy(existing.getCreatedBy())
                .updatedBy(existing.getCreatedBy())
                .build())
            .flatMap(repo::guardar);
    }

    @Override
    public Flux<Gasto> listarTodosPorControl(UUID controlId) {
        return repo.porControl(controlId);
    }

    @Override
    public Mono<Map<String, Object>> resumen(UUID controlId) {
        return controlRepo.porId(controlId)
            .switchIfEmpty(Mono.error(new RecursoNoEncontradoExcepcion("Control no encontrado: " + controlId)))
            .flatMap(control -> {
                Flux<GrupoMiembro> membersFlux = control.getGrupoId() != null
                    ? grupoMiembroRepo.porGrupo(control.getGrupoId())
                    : Flux.empty();
                return membersFlux.collectList()
                    .flatMap(members -> {
                        List<UUID> memberIds = new ArrayList<>();
                        for (GrupoMiembro m : members) memberIds.add(m.getUsuarioId());
                        if (memberIds.isEmpty() && control.getCreatedBy() != null) memberIds.add(control.getCreatedBy());

                        // Porcentajes desde tabla control_asignaciones (incluye eliminados para histórico)
                        return asignacionRepo.porControlTodos(controlId).collectList()
                            .flatMap(asignaciones -> {
                                Map<String, Double> pctMap = new HashMap<>();
                                List<UUID> allIds = new ArrayList<>(memberIds);
                                for (ControlAsignacion ca : asignaciones) {
                                    pctMap.put(ca.getUsuarioId().toString(), ca.getPorcentaje().doubleValue());
                                    if (!allIds.contains(ca.getUsuarioId())) allIds.add(ca.getUsuarioId());
                                }

                                return repo.porControl(controlId).collectList()
                                    .flatMap(gastos -> {
                                        // Incluir usuarios asignados en gastos
                                        for (Gasto g : gastos) {
                                            if (g.getUsuarioId() != null && !allIds.contains(g.getUsuarioId())) allIds.add(g.getUsuarioId());
                                        }
                                        if (allIds.isEmpty() && control.getCreatedBy() != null) allIds.add(control.getCreatedBy());

                                        Map<UUID, BigDecimal> totals = new HashMap<>();
                                        for (UUID uid : allIds) totals.put(uid, BigDecimal.ZERO);

                                        double totalPct = pctMap.values().stream().mapToDouble(Double::doubleValue).sum();

                                        for (Gasto g : gastos) {
                                            BigDecimal monto = g.getMonto() != null ? g.getMonto() : BigDecimal.ZERO;
                                            if (g.getUsuarioId() != null) {
                                                totals.merge(g.getUsuarioId(), monto, BigDecimal::add);
                                            } else if (totalPct > 0) {
                                                for (UUID uid : allIds) {
                                                    double pct = pctMap.containsKey(uid.toString()) ? pctMap.get(uid.toString()) : 0;
                                                    BigDecimal part = monto.multiply(BigDecimal.valueOf(pct / totalPct));
                                                    totals.merge(uid, part, BigDecimal::add);
                                                }
                                            }
                                        }

                                        // Enriquecer con datos del usuario
                                        List<Mono<Map<String, Object>>> monos = new ArrayList<>();
                                        for (UUID uid : allIds) {
                                            monos.add(Mono.fromCallable(() -> {
                                                java.net.http.HttpClient client = java.net.http.HttpClient.newBuilder()
                                                    .proxy(java.net.ProxySelector.of(null)).build();
                                                java.net.http.HttpRequest req = java.net.http.HttpRequest.newBuilder(
                                                    java.net.URI.create(usuariosBaseUrl + "/api/usuarios/" + uid)).GET().build();
                                                java.net.http.HttpResponse<String> resp = client.send(req, java.net.http.HttpResponse.BodyHandlers.ofString());
                                                String body = resp.body();
                                                String nombres = extractField(body, "nombres");
                                                String apellidos = extractField(body, "apellidos");
                                                String username = extractField(body, "username");
                                                Map<String, Object> m = new HashMap<>();
                                                m.put("usuarioId", uid.toString());
                                                m.put("total", totals.get(uid));
                                                m.put("nombre", (nombres + " " + apellidos).trim().isEmpty() ? username : (nombres + " " + apellidos).trim());
                                                return m;
                                            }).subscribeOn(reactor.core.scheduler.Schedulers.boundedElastic()));
                                        }
                                        if (monos.isEmpty()) {
                                            Map<String, Object> result = new HashMap<>();
                                            result.put("totales", new ArrayList<>());
                                            return Mono.just(result);
                                        }
                                        return Mono.zip(monos, (Object[] arr) -> {
                                            List<Map<String, Object>> list = new ArrayList<>();
                                            for (Object o : arr) list.add((Map<String, Object>) o);
                                            Map<String, Object> result = new HashMap<>();
                                            result.put("totales", list);
                                            return result;
                                        });
                                    });
                            });
                    });
            });
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
    public Mono<Void> eliminar(UUID id) {
        return repo.porId(id)
            .switchIfEmpty(Mono.error(new RecursoNoEncontradoExcepcion("Gasto no encontrado: " + id)))
            .flatMap(existing -> controlRepo.porId(existing.getControlId())
                .switchIfEmpty(Mono.error(new RecursoNoEncontradoExcepcion("Control no encontrado")))
                .flatMap(control -> {
                    if ("COMPLETADO".equals(control.getEstadoControl())) {
                        return Mono.error(new IllegalStateException("No se puede modificar un control completado"));
                    }
                    return repo.eliminar(id);
                }));
    }
}