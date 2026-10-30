package win.huansha.moneipe.gastos.dominio.modelo;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class Gasto {
    UUID id;
    UUID controlId;
    String descripcion;
    BigDecimal monto;
    LocalDate fecha;
    UUID usuarioId;
    String asignaciones;
    Instant createdAt;
    Instant updatedAt;
    UUID createdBy;
    UUID updatedBy;
}