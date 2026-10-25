package win.huansha.moneipe.gastos.infraestructura.adaptador.entrada.web;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import win.huansha.moneipe.gastos.aplicacion.servicio.GrupoServicio;
import win.huansha.moneipe.gastos.dominio.modelo.Grupo;

import java.util.UUID;

@RestController
@RequestMapping("/api/gastos/grupos")
@RequiredArgsConstructor
public class GrupoController {

    private final GrupoServicio grupoServicio;

    @GetMapping
    public Flux<Grupo> listarMios() {
        return usuarioActual().flatMapMany(grupoServicio::listarMios);
    }

    private Mono<UUID> usuarioActual() {
        return ReactiveSecurityContextHolder.getContext()
            .map(ctx -> ctx.getAuthentication())
            .cast(JwtAuthenticationToken.class)
            .map(auth -> UUID.fromString(auth.getToken().getSubject()));
    }
}
