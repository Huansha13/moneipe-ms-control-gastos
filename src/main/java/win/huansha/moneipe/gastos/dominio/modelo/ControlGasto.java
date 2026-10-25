package win.huansha.moneipe.gastos.dominio.modelo;

import java.time.Instant;
import java.util.UUID;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ControlGasto {
    UUID id;
    String titulo;
    String descripcion;
    UUID grupoId;
    Instant createdAt;
    Instant updatedAt;
    UUID createdBy;
    UUID updatedBy;
}
