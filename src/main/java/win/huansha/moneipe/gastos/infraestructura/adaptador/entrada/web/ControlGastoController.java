package win.huansha.moneipe.gastos.infraestructura.adaptador.entrada.web;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import win.huansha.moneipe.gastos.aplicacion.servicio.ControlGastoServicio;
import win.huansha.moneipe.gastos.aplicacion.servicio.GastoServicio;
import win.huansha.moneipe.gastos.aplicacion.servicio.GrupoServicio;
import win.huansha.moneipe.gastos.dominio.modelo.AuditoriaGasto;
import win.huansha.moneipe.gastos.dominio.modelo.ControlAsignacion;
import win.huansha.moneipe.gastos.dominio.modelo.ControlGasto;
import win.huansha.moneipe.gastos.dominio.modelo.Gasto;
import win.huansha.moneipe.gastos.dominio.modelo.Grupo;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/gastos/controles")
@RequiredArgsConstructor
public class ControlGastoController {

    private final ControlGastoServicio servicio;
    private final GastoServicio gastoServicio;
    private final GrupoServicio grupoServicio;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<ControlGasto> crear(@RequestHeader("X-User-Id") String userId, @RequestBody Map<String, String> body) {
        return usuarioActual(userId).flatMap(uid ->
            servicio.crear(uid, body.get("titulo"), body.get("descripcion"), body.get("tipo"), body.get("moneda")));
    }

    @GetMapping("/{id}")
    public Mono<ControlGasto> porId(@PathVariable UUID id) {
        return servicio.porId(id);
    }

    @GetMapping
    public Flux<ControlGasto> listar(@RequestHeader("X-User-Id") String userId,
                                    @org.springframework.web.bind.annotation.RequestParam(required = false) String tipo) {
        return usuarioActual(userId).flatMapMany(uid -> servicio.listarMios(uid, tipo));
    }

    @PutMapping("/{id}")
    public Mono<ControlGasto> actualizar(@PathVariable UUID id, @RequestHeader("X-User-Id") String userId, @RequestBody Map<String, Object> body) {
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> raw = body.get("asignaciones") != null
            ? (List<Map<String, Object>>) body.get("asignaciones") : null;
        final List<ControlAsignacion> asignaciones;
        if (raw != null) {
            List<ControlAsignacion> tmp = new ArrayList<>();
            for (Map<String, Object> m : raw) {
                tmp.add(ControlAsignacion.builder()
                    .controlId(id)
                    .usuarioId(UUID.fromString(m.get("usuarioId").toString()))
                    .porcentaje(new java.math.BigDecimal(m.get("porcentaje").toString()))
                    .build());
            }
            asignaciones = tmp;
        } else {
            asignaciones = null;
        }
        return usuarioActual(userId).flatMap(uid ->
            servicio.actualizar(id, uid, (String) body.get("titulo"), (String) body.get("descripcion"), asignaciones));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> eliminar(@PathVariable UUID id) {
        return servicio.eliminar(id);
    }

    @PutMapping("/{id}/estado")
    public Mono<ControlGasto> cambiarEstado(@PathVariable UUID id, @RequestBody Map<String, String> body) {
        return servicio.cambiarEstado(id, body.get("estado"));
    }

    @PostMapping("/{id}/invitar")
    public Mono<Grupo> invitar(@PathVariable UUID id, @RequestBody Map<String, String> body) {
        UUID usuarioId = UUID.fromString(body.get("usuarioId"));
        String rol = body.getOrDefault("rol", "MEMBER");
        return grupoServicio.invitar(id, usuarioId, rol);
    }

    @GetMapping("/{id}/gastos")
    public Flux<Gasto> listarGastos(@PathVariable UUID id) {
        return gastoServicio.listarPorControl(id);
    }

    @GetMapping("/{id}/gastos/todos")
    public Flux<Gasto> listarTodosGastos(@PathVariable UUID id) {
        return gastoServicio.listarTodosPorControl(id);
    }

    @GetMapping("/{id}/resumen")
    public Mono<Map<String, Object>> resumen(@PathVariable UUID id) {
        return gastoServicio.resumen(id);
    }

    @PostMapping("/{id}/gastos")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Gasto> crearGasto(@PathVariable UUID id, @RequestHeader("X-User-Id") String userId, @RequestBody Map<String, Object> body) {
        BigDecimal monto = new BigDecimal(body.getOrDefault("monto", "0").toString());
        LocalDate fecha = body.get("fecha") != null ? LocalDate.parse(body.get("fecha").toString()) : LocalDate.now();
        UUID usuarioId = body.get("usuarioId") != null && !body.get("usuarioId").toString().isBlank()
            ? UUID.fromString(body.get("usuarioId").toString()) : null;
        String asignaciones = body.get("asignaciones") != null ? body.get("asignaciones").toString() : null;
        return usuarioActual(userId).flatMap(uid ->
            gastoServicio.crear(id, uid, (String) body.get("descripcion"), monto, fecha, usuarioId, asignaciones));
    }

    @DeleteMapping("/{id}/gastos/{gastoId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> eliminarGasto(@PathVariable UUID gastoId) {
        return gastoServicio.eliminar(gastoId);
    }

    @PutMapping("/gastos/{gastoId}")
    public Mono<Gasto> actualizarGasto(@PathVariable UUID gastoId, @RequestBody Map<String, Object> body) {
        BigDecimal monto = body.get("monto") != null ? new BigDecimal(body.get("monto").toString()) : null;
        LocalDate fecha = body.get("fecha") != null ? LocalDate.parse(body.get("fecha").toString()) : null;
        UUID usuarioId = body.get("usuarioId") != null && !body.get("usuarioId").toString().isBlank()
            ? UUID.fromString(body.get("usuarioId").toString()) : null;
        String asignaciones = body.get("asignaciones") != null ? body.get("asignaciones").toString() : null;
        return gastoServicio.actualizar(gastoId, (String) body.get("descripcion"), monto, fecha, usuarioId, asignaciones);
    }

    @GetMapping("/{id}/auditoria")
    public Flux<AuditoriaGasto> auditoria(@PathVariable UUID id) {
        return servicio.auditoria(id);
    }

    @GetMapping("/estadisticas/mensual")
    public Flux<java.util.Map<String, Object>> estadisticasMensual(@RequestHeader("X-User-Id") String userId) {
        return usuarioActual(userId).flatMapMany(servicio::gastosPorMes);
    }

    @GetMapping("/{id}/asignaciones")
    public Flux<ControlAsignacion> asignaciones(@PathVariable UUID id) {
        return servicio.asignaciones(id);
    }

    private Mono<UUID> usuarioActual(@RequestHeader("X-User-Id") String userId) {
        return Mono.just(UUID.fromString(userId));
    }
}
