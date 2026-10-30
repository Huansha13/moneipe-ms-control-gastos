package win.huansha.moneipe.gastos.infraestructura.adaptador.salida.persistencia;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import java.util.UUID;

public interface AuditoriaGastoR2dbcRepository extends ReactiveCrudRepository<AuditoriaGastoEntity, UUID> {
}