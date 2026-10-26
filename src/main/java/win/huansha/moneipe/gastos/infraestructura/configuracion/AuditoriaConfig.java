package win.huansha.moneipe.gastos.infraestructura.configuracion;

import java.util.UUID;

import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.ReactiveAuditorAware;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.r2dbc.config.EnableR2dbcAuditing;

@Configuration
@EnableR2dbcAuditing(auditorAwareRef = "auditorAware")
public class AuditoriaConfig {

    @Bean
    public ReactiveAuditorAware<UUID> auditorAware() {
        // La auditoría se gestiona manualmente en los servicios (createdBy/updatedBy).
        // El MS confía en el gateway para la identidad del usuario.
        return () -> reactor.core.publisher.Mono.empty();
    }
}
