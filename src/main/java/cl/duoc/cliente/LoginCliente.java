package cl.duoc.cliente;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import cl.duoc.ms_tiendas.dto.UsuarioDto;
import lombok.RequiredArgsConstructor;

// Cliente HTTP que se comunica con ms-login
// Usa RestTemplate (libreria de Spring) para hacer llamadas HTTP REST
// Este patron se llama "cliente HTTP" y es como los microservicios se hablan entre si



@Component
@RequiredArgsConstructor
public class LoginCliente {




    // RestTemplate es el objeto que hace las peticiones HTTP
    // Se configura como Bean en ConfiguracionSeguridad
    private final RestTemplate restTemplate;

    // URL base de ms-login, se lee desde application.properties
    // Asi si cambia el puerto o el host, solo cambiamos el properties
    @Value("${ms-login.url}")
    private String urlMsLogin;

    // ----------------------------------------------------------------
    // Consulta los datos de un usuario en ms-login por su ID
    // Recibe el token JWT para que ms-login sepa que somos un MS autorizado
    // ----------------------------------------------------------------
    public UsuarioDto obtenerUsuarioPorId(String idUsuario, String tokenJwt) {
        try {
            // Construir la URL completa del endpoint de ms-login
            String url = urlMsLogin + "/api/usuarios/" + idUsuario;

            // Crear los headers HTTP con el token JWT
            // Sin este header, ms-login rechazara la peticion con 403
            HttpHeaders headers = new HttpHeaders();
            headers.set("Autorizacion", tokenJwt); // tokenJwt ya viene con "Bearer eyJ..."

            // Crear la entidad HTTP (headers + body vacio para GET)
            HttpEntity<Void> peticion = new HttpEntity<>(headers);

            // Hacer la peticion GET y mapear la respuesta a UsuarioDTO
            ResponseEntity<UsuarioDto> respuesta = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    peticion,
                    UsuarioDto.class
            );

            // Retornar el cuerpo de la respuesta
            return respuesta.getBody();

        } catch (Exception e) {
            // Si ms-login no responde o hay un error, retornamos null
            // El servicio que llama este metodo debe manejar el null
            System.out.println("No se pudo consultar ms-login para usuario " + idUsuario + ": " + e.getMessage());
            return null;
        }
    }
}
