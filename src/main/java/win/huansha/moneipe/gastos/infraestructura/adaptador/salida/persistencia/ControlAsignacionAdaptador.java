package win.huansha.moneipe.gastos.infraestructura.adaptador.salida.persistencia;

import lombok.RequiredArgsConstructor;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import win.huansha.moneipe.gastos.dominio.modelo.ControlAsignacion;
import win.huansha.moneipe.gastos.dominio.puerto.salida.ControlAsignacionRepositorio;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ControlAsignacionAdaptador implements ControlAsignacionRepositorio {

    private final DatabaseClient databaseClient;

    @Override
    public Flux<ControlAsignacion> guardarTodos(UUID controlId, List<ControlAsignacion> asignaciones) {
        // Marcar existentes como eliminados (borrado logico)
        return databaseClient.sql("UPDATE control_asignaciones SET estado = 'ELIMINADO', deleted_at = NOW() WHERE control_id = :cid")
            .bind("cid", controlId)
            .fetch().rowsUpdated()
            .thenMany(Flux.fromIterable(asignaciones))
            .flatMap(a -> databaseClient.sql(
                "INSERT INTO control_asignaciones (control_id, usuario_id, porcentaje, estado) VALUES (:cid, :uid, :pct, 'ACTIVO') " +
                "ON CONFLICT (control_id, usuario_id) DO UPDATE SET porcentaje = :pct, estado = 'ACTIVO', deleted_at = NULL")
                .bind("cid", controlId)
                .bind("uid", a.getUsuarioId())
                .bind("pct", a.getPorcentaje())
                .fetch().rowsUpdated()
                .thenReturn(a));
    }

    @Override
    public Flux<ControlAsignacion> porControl(UUID controlId) {
        return databaseClient.sql("SELECT control_id, usuario_id, porcentaje, estado FROM control_asignaciones WHERE control_id = :cid AND estado = 'ACTIVO'")
            .bind("cid", controlId)
            .map((row, meta) -> ControlAsignacion.builder()
                .controlId(row.get("control_id", UUID.class))
                .usuarioId(row.get("usuario_id", UUID.class))
                .porcentaje(row.get("porcentaje", BigDecimal.class))
                .build())
            .all();
    }

    @Override
    public Flux<ControlAsignacion> porControlTodos(UUID controlId) {
        return databaseClient.sql("SELECT control_id, usuario_id, porcentaje, estado FROM control_asignaciones WHERE control_id = :cid")
            .bind("cid", controlId)
            .map((row, meta) -> ControlAsignacion.builder()
                .controlId(row.get("control_id", UUID.class))
                .usuarioId(row.get("usuario_id", UUID.class))
                .porcentaje(row.get("porcentaje", BigDecimal.class))
                .build())
            .all();
    }

    @Override
    public Mono<Void> eliminarPorControl(UUID controlId) {
        return databaseClient.sql("DELETE FROM control_asignaciones WHERE control_id = :cid")
            .bind("cid", controlId)
            .fetch().rowsUpdated().then();
    }
}