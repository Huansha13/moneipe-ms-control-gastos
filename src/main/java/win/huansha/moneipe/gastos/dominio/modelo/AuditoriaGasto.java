package win.huansha.moneipe.gastos.dominio.modelo;

import java.time.Instant;
import java.util.UUID;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class AuditoriaGasto {
    UUID id;
    UUID controlId;
    UUID usuarioId;
    String accion;
    String entidad;
    UUID entidadId;
    String valorAnterior;
    String valorNuevo;
    Instant fecha;
}