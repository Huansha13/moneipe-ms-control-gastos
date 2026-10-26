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
import win.huansha.moneipe.gastos.aplicacion.servicio.GrupoServicio;
import win.huansha.moneipe.gastos.dominio.modelo.ControlGasto;
import win.huansha.moneipe.gastos.dominio.modelo.Grupo;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/gastos/controles")
@RequiredArgsConstructor
public class ControlGastoController {

    private final ControlGastoServicio servicio;
    private final GrupoServicio grupoServicio;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<ControlGasto> crear(@RequestHeader("X-User-Id") String userId, @RequestBody Map<String, String> body) {
        return usuarioActual(userId).flatMap(uid ->
            servicio.crear(uid, body.get("titulo"), body.get("descripcion")));
    }

    @GetMapping("/{id}")
    public Mono<ControlGasto> porId(@PathVariable UUID id) {
        return servicio.porId(id);
    }

    @GetMapping
    public Flux<ControlGasto> listar(@RequestHeader("X-User-Id") String userId) {
        return usuarioActual(userId).flatMapMany(servicio::listarMios);
    }

    @PutMapping("/{id}")
    public Mono<ControlGasto> actualizar(@PathVariable UUID id, @RequestHeader("X-User-Id") String userId, @RequestBody Map<String, String> body) {
        return usuarioActual(userId).flatMap(uid ->
            servicio.actualizar(id, uid, body.get("titulo"), body.get("descripcion")));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> eliminar(@PathVariable UUID id) {
        return servicio.eliminar(id);
    }

    @PostMapping("/{id}/invitar")
    public Mono<Grupo> invitar(@PathVariable UUID id, @RequestBody Map<String, String> body) {
        UUID usuarioId = UUID.fromString(body.get("usuarioId"));
        String rol = body.getOrDefault("rol", "MEMBER");
        return grupoServicio.invitar(id, usuarioId, rol);
    }

    private Mono<UUID> usuarioActual(@RequestHeader("X-User-Id") String userId) {
        return Mono.just(UUID.fromString(userId));
    }
}
