package win.huansha.moneipe.gastos.infraestructura.configuracion;

import java.util.UUID;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.r2dbc.config.EnableR2dbcAuditing;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

@Configuration
@EnableR2dbcAuditing(auditorAwareRef = "auditorAware")
public class AuditoriaConfig {

    @Bean
    public org.springframework.data.domain.ReactiveAuditorAware<UUID> auditorAware() {
        return () -> ReactiveSecurityContextHolder.getContext()
            .map(ctx -> ctx.getAuthentication())
            .filter(Authentication::isAuthenticated)
            .map(auth -> {
                if (auth.getPrincipal() instanceof Jwt jwt) {
                    try {
                        return UUID.fromString(jwt.getSubject());
                    } catch (IllegalArgumentException ignored) {
                        return null;
                    }
                }
                return null;
            });
    }
}
