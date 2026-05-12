package cl.duoc.ms_tiendas.configuracion;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.security.Key;
import java.util.List;

@Component
public class FiltroJwt OncePerRequestFilter{

// La misma clave secreta que usa ms-login para firmar los tokens
    // Debe ser IDENTICA en todos los microservicios (esta en application.properties)
    @Value("${jwt.secret}")
    private String secreto;

    
    protected void doFilterInternal(HttpServletRequest peticion,
                                    HttpServletResponse respuesta,
                                    FilterChain cadenaFiltros)
            throws ServletException, IOException {

        // Leer el header Authorization del request entrante
        String headerAutorizacion = peticion.getHeader("Authorization");

        // Si no trae token, dejar pasar y Spring Security rechazara el request
        if (headerAutorizacion == null || !headerAutorizacion.startsWith("Bearer ")) {
            cadenaFiltros.doFilter(peticion, respuesta);
            return;
        }

        // Quitar el prefijo "Bearer " para quedarnos solo con el token
        String token = headerAutorizacion.substring(7);

        try {
            // Validar y decodificar el token con la clave secreta compartida
            Key clave = Keys.hmacShaKeyFor(secreto.getBytes());
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(clave)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            // Extraer datos del usuario desde el payload del token
            String email     = claims.getSubject();
            String rol       = (String) claims.get("rol");
            Long   idUsuario = ((Number) claims.get("idUsuario")).longValue();

            // Guardar el idUsuario como atributo del request
            // El controlador lo lee con @RequestHeader("X-Usuario-Id")
            peticion.setAttribute("X-Usuario-Id", idUsuario);

            // Registrar la autenticacion en Spring Security
            UsernamePasswordAuthenticationToken autenticacion = new UsernamePasswordAuthenticationToken(
                    email,
                    null,
                    List.of(new SimpleGrantedAuthority("ROLE_" + rol))
            );
            SecurityContextHolder.getContext().setAuthentication(autenticacion);

        } catch (JwtException | IllegalArgumentException e) {
            // Token invalido o expirado
            System.out.println("Token JWT invalido en ms-tiendas: " + e.getMessage());
        }

        cadenaFiltros.doFilter(peticion, respuesta);
    }

}
