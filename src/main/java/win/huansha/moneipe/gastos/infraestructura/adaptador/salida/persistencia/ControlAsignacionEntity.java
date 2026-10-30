package win.huansha.moneipe.gastos.infraestructura.adaptador.salida.persistencia;

import java.math.BigDecimal;
import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("control_asignaciones")
public class ControlAsignacionEntity {
    @Id
    private UUID controlId; // composite PK handled manually
    private UUID usuarioId;
    private BigDecimal porcentaje;

    public ControlAsignacionEntity() {}

    public UUID getControlId() { return controlId; }
    public void setControlId(UUID controlId) { this.controlId = controlId; }
    public UUID getUsuarioId() { return usuarioId; }
    public void setUsuarioId(UUID usuarioId) { this.usuarioId = usuarioId; }
    public BigDecimal getPorcentaje() { return porcentaje; }
    public void setPorcentaje(BigDecimal porcentaje) { this.porcentaje = porcentaje; }
}