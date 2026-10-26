package win.huansha.moneipe.gastos.infraestructura.adaptador.salida.persistencia;

import java.time.Instant;
import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("grupo_miembros")
public class GrupoMiembroEntity {
    private UUID grupoId;
    private UUID usuarioId;
    private String rol;
    private Instant fechaIngreso;
    private String estado;
    private Instant deletedAt;

    public GrupoMiembroEntity() {}
    public UUID getGrupoId() { return grupoId; }
    public void setGrupoId(UUID grupoId) { this.grupoId = grupoId; }
    public UUID getUsuarioId() { return usuarioId; }
    public void setUsuarioId(UUID usuarioId) { this.usuarioId = usuarioId; }
    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }
    public Instant getFechaIngreso() { return fechaIngreso; }
    public void setFechaIngreso(Instant fechaIngreso) { this.fechaIngreso = fechaIngreso; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public Instant getDeletedAt() { return deletedAt; }
    public void setDeletedAt(Instant deletedAt) { this.deletedAt = deletedAt; }
}
