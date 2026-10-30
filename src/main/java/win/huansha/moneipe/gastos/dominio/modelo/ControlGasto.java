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
    String tipo;
    String moneda;
    String estadoControl;
    String asignaciones;
    UUID grupoId;
    int cantidadGastos;
    Instant createdAt;
    Instant updatedAt;
    UUID createdBy;
    UUID updatedBy;
}
