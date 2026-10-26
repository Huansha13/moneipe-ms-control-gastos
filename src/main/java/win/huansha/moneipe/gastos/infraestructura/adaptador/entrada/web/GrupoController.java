package win.huansha.moneipe.gastos.infraestructura.adaptador.entrada.web;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import win.huansha.moneipe.gastos.aplicacion.servicio.GrupoServicio;
import win.huansha.moneipe.gastos.dominio.modelo.Grupo;
import win.huansha.moneipe.gastos.dominio.modelo.GrupoMiembro;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/gastos")
@RequiredArgsConstructor
public class GrupoController {

    private final GrupoServicio grupoServicio;

    @PostMapping("/grupos")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Grupo> crearGrupo(@RequestHeader("X-User-Id") String userId, @RequestBody Map<String, Object> body) {
        String titulo = (String) body.get("titulo");
        String descripcion = (String) body.get("descripcion");
        @SuppressWarnings("unchecked")
        List<String> memberIdsRaw = (List<String>) body.getOrDefault("memberIds", List.of());
        List<UUID> memberIds = memberIdsRaw.stream().map(UUID::fromString).toList();

        return usuarioActual(userId).flatMap(uid ->
            grupoServicio.crear(uid, titulo, descripcion, memberIds));
    }

    @GetMapping("/grupos")
    public Flux<Grupo> listarMios(@RequestHeader("X-User-Id") String userId) {
        return usuarioActual(userId).flatMapMany(grupoServicio::listarMios);
    }

    @GetMapping("/grupos/{id}/miembros")
    public Flux<GrupoMiembro> miembros(@PathVariable UUID id) {
        return grupoServicio.miembros(id);
    }

    @DeleteMapping("/grupos/{id}/salir")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> salir(@PathVariable UUID id, @RequestHeader("X-User-Id") String userId) {
        return usuarioActual(userId).flatMap(uid -> grupoServicio.salir(id, uid));
    }

    private Mono<UUID> usuarioActual(String userId) {
        return Mono.just(UUID.fromString(userId));
    }
}
