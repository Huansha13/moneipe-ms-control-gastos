package win.huansha.moneipe.gastos.dominio.modelo;

import java.time.Instant;
import java.util.UUID;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class Grupo {
    UUID id;
    String nombre;
    String descripcion;
    UUID controlId;
    Instant createdAt;
    Instant updatedAt;
    UUID createdBy;
    UUID updatedBy;
}
