package cl.duoc.ms_tiendas.clientes;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import cl.duoc.ms_tiendas.dto.LocalizacionDTO;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class LocalizacionCliente {

    private final RestTemplate restTemplate;

    // URL base de ms-localizacion, configurada en application.properties
    @Value("${ms-localizacion.url}")
    private String urlMsLocalizacion;

    // ----------------------------------------------------------------
    // Consulta la localizacion de una tienda por su ID
    // ----------------------------------------------------------------
    public LocalizacionDTO obtenerLocalizacionDeTienda(Long idTienda, String tokenJwt) {
        try {
            // Armar la URL con el ID de la tienda
            String url = urlMsLocalizacion + "/api/localizacion/tienda/" + idTienda;

            // Preparar el header de autorizacion con el JWT
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", tokenJwt);

            HttpEntity<Void> peticion = new HttpEntity<>(headers);

            // Hacer la llamada al otro microservicio
            ResponseEntity<LocalizacionDTO> respuesta = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    peticion,
                    LocalizacionDTO.class
            );

            return respuesta.getBody();

        } catch (Exception e) {
            // Si ms-localizacion no responde, continuar sin la direccion
            System.out.println("Error al consultar ms-localizacion: " + e.getMessage());
            return null;
        }
    }
}
