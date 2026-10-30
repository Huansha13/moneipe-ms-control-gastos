package win.huansha.moneipe.gastos.dominio.modelo;

import java.math.BigDecimal;
import java.util.UUID;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ControlAsignacion {
    UUID controlId;
    UUID usuarioId;
    BigDecimal porcentaje;
}