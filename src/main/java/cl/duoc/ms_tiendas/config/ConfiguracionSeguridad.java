package cl.duoc.ms_tiendas.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod; // <-- ¡NUEVO IMPORT!
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.client.RestTemplate;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class ConfiguracionSeguridad {

    private final FiltroJwt filtroJwt;

    @Bean
    public SecurityFilterChain configurarSeguridad(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(sesion ->
                sesion.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Permitimos que cualquiera consulte (GET) las tiendas
                .requestMatchers(HttpMethod.GET, "/api/tiendas/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/tiendas/*/resumen").permitAll()
                
                // Todos los demas endpoints requieren token JWT valido
                .anyRequest().authenticated()
            )
            .addFilterBefore(filtroJwt, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // RestTemplate es el cliente HTTP que usamos para llamar a otros microservicios
    // Se declara como Bean para que Spring lo pueda inyectar en LoginCliente
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}