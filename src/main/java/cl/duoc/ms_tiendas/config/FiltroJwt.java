package cl.duoc.ms_tiendas.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

import javax.crypto.SecretKey;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Component
public class FiltroJwt extends OncePerRequestFilter {

    // El mismo secret que usa ms-login para firmar los tokens
    @Value("${jwt.secret}")
    private String secreto;

    @Override
    protected void doFilterInternal(HttpServletRequest peticion,
                                    HttpServletResponse respuesta,
                                    FilterChain cadenaFiltros)
            throws ServletException, IOException {

        String headerAutorizacion = peticion.getHeader("Authorization");

        // Si no trae token, dejar pasar (Spring Security lo rechazara solo)
        if (headerAutorizacion == null || !headerAutorizacion.startsWith("Bearer ")) {
            cadenaFiltros.doFilter(peticion, respuesta);
            return;
        }

        String token = headerAutorizacion.substring(7);

        try {
            // CAMBIO: API de jjwt 0.12.x
            // Antes (0.11.x): Jwts.parserBuilder().setSigningKey(clave).build()
            // Ahora (0.12.x): Jwts.parser().verifyWith(clave).build()
            SecretKey clave = Keys.hmacShaKeyFor(secreto.getBytes());

            Claims claims = Jwts.parser()
                    .verifyWith(clave)
                    .build()
                    .parseClaimsJws(token)
                    .getPayload();   // CAMBIO: antes era .getBody()

            String email = claims.getSubject();
            String rol   = (String) claims.get("rol");

            // CAMBIO CRÍTICO: el claim se llama "id", no "idUsuario"
            // En ms-login se guarda así: .claim("id", guardado.getId())
            Long idUsuario = ((Number) claims.get("id")).longValue();

            // Guardar el id en el request para que el controlador lo use
            // El controlador lo lee con @RequestAttribute("X-Usuario-Id")
            peticion.setAttribute("X-Usuario-Id", idUsuario);

            // Registrar la autenticacion en Spring Security con el rol del usuario
            UsernamePasswordAuthenticationToken autenticacion =
                    new UsernamePasswordAuthenticationToken(
                            email,
                            null,
                            List.of(new SimpleGrantedAuthority("ROLE_" + rol))
                    );
            SecurityContextHolder.getContext().setAuthentication(autenticacion);

        } catch (JwtException | IllegalArgumentException e) {
            // Token invalido, expirado o mal formado → Spring Security rechazara el request
            System.out.println("Token JWT invalido en ms-tiendas: " + e.getMessage());
        }

        cadenaFiltros.doFilter(peticion, respuesta);
    }
}
