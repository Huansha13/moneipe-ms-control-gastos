package win.huansha.moneipe.gastos.dominio.puerto.entrada;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import win.huansha.moneipe.gastos.dominio.modelo.Gasto;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public interface GastoCasoUso {
    Mono<Gasto> crear(UUID controlId, UUID creadoPor, String descripcion, BigDecimal monto, LocalDate fecha, UUID usuarioId, String asignaciones);
    Mono<Gasto> actualizar(UUID id, String descripcion, BigDecimal monto, LocalDate fecha, UUID usuarioId, String asignaciones);
    Flux<Gasto> listarPorControl(UUID controlId);
    Flux<Gasto> listarTodosPorControl(UUID controlId);
    Mono<java.util.Map<String, Object>> resumen(UUID controlId);
    Mono<Void> eliminar(UUID id);
}