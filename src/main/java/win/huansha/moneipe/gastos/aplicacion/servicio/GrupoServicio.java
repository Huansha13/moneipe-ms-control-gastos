package win.huansha.moneipe.gastos.aplicacion.servicio;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import win.huansha.moneipe.gastos.dominio.excepcion.RecursoNoEncontradoExcepcion;
import win.huansha.moneipe.gastos.dominio.modelo.ControlGasto;
import win.huansha.moneipe.gastos.dominio.modelo.Grupo;
import win.huansha.moneipe.gastos.dominio.modelo.GrupoMiembro;
import win.huansha.moneipe.gastos.dominio.puerto.salida.ControlGastoRepositorio;
import win.huansha.moneipe.gastos.dominio.puerto.salida.GrupoMiembroRepositorio;
import win.huansha.moneipe.gastos.dominio.puerto.salida.GrupoRepositorio;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GrupoServicio {

    private final ControlGastoRepositorio controlRepo;
    private final GrupoRepositorio grupoRepo;
    private final GrupoMiembroRepositorio miembroRepo;

    @Transactional
    public Mono<Grupo> invitar(UUID controlId, UUID usuarioId, String rol) {
        String rolFinal = (rol != null && !rol.isBlank()) ? rol : "MEMBER";

        return controlRepo.porId(controlId)
            .switchIfEmpty(Mono.error(new RecursoNoEncontradoExcepcion("Control no encontrado: " + controlId)))
            .flatMap(control -> {
                // Si el control ya tiene grupo, usarlo; si no, crear uno
                if (control.getGrupoId() != null) {
                    return grupoRepo.porId(control.getGrupoId())
                        .flatMap(grupo -> agregarMiembro(grupo, usuarioId, rolFinal)
                            .thenReturn(grupo));
                }
                // Crear grupo nuevo
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
                        // Vincular control con grupo
                        ControlGasto controlActualizado = ControlGasto.builder()
                            .id(control.getId())
                            .titulo(control.getTitulo())
                            .descripcion(control.getDescripcion())
                            .grupoId(grupoGuardado.getId())
                            .createdAt(control.getCreatedAt())
                            .updatedAt(Instant.now())
                            .createdBy(control.getCreatedBy())
                            .updatedBy(control.getCreatedBy())
                            .build();
                        return controlRepo.guardar(controlActualizado)
                            .then(agregarMiembro(grupoGuardado, usuarioId, rolFinal))
                            .thenReturn(grupoGuardado);
                    });
            });
    }

    private Mono<Void> agregarMiembro(Grupo grupo, UUID usuarioId, String rol) {
        return miembroRepo.existe(grupo.getId(), usuarioId)
            .flatMap(yaExiste -> {
                if (yaExiste) return Mono.empty();
                GrupoMiembro miembro = GrupoMiembro.builder()
                    .grupoId(grupo.getId())
                    .usuarioId(usuarioId)
                    .rol(rol)
                    .fechaIngreso(Instant.now())
                    .build();
                return miembroRepo.guardar(miembro).then();
            });
    }

    public Mono<Grupo> porControlId(UUID controlId) {
        return grupoRepo.porControlId(controlId);
    }

    public Flux<Grupo> listarMios(UUID usuarioId) {
        return miembroRepo.porUsuario(usuarioId)
            .map(GrupoMiembro::getGrupoId)
            .distinct()
            .flatMap(grupoRepo::porId);
    }
}
