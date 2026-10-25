package win.huansha.moneipe.gastos.infraestructura.adaptador.entrada.web;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
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
import win.huansha.moneipe.gastos.dominio.modelo.ControlGasto;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/gastos/controles")
@RequiredArgsConstructor
public class ControlGastoController {

    private final ControlGastoServicio servicio;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<ControlGasto> crear(@RequestBody Map<String, String> body) {
        return usuarioActual().flatMap(uid ->
            servicio.crear(body.get("titulo"), body.get("descripcion")));
    }

    @GetMapping("/{id}")
    public Mono<ControlGasto> porId(@PathVariable UUID id) {
        return servicio.porId(id);
    }

    @GetMapping
    public Flux<ControlGasto> listar() {
        return usuarioActual().flatMapMany(servicio::listarMios);
    }

    @PutMapping("/{id}")
    public Mono<ControlGasto> actualizar(@PathVariable UUID id, @RequestBody Map<String, String> body) {
        return servicio.actualizar(id, body.get("titulo"), body.get("descripcion"));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> eliminar(@PathVariable UUID id) {
        return servicio.eliminar(id);
    }

    private Mono<UUID> usuarioActual() {
        return ReactiveSecurityContextHolder.getContext()
            .map(ctx -> ctx.getAuthentication())
            .cast(JwtAuthenticationToken.class)
            .map(auth -> UUID.fromString(auth.getToken().getSubject()));
    }
}
