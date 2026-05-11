package cl.duoc.cliente;

// Cliente HTTP que se comunica con ms-inventario

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import lombok.*;

// Consulta cuantos productos activos tiene una tienda para mostrar en el perfil
@Component
@NoArgsConstructor
@AllArgsConstructor
public class InventarioCliente {

    private final RestTemplate restTemplate;

    // URL base de ms-inventario, configurada en application.properties
    @Value("${ms-inventario.url}")
    private String urlMsInventario;

    // ----------------------------------------------------------------
    // Consulta el resumen de inventario de una tienda
    // ----------------------------------------------------------------
    public InventarioResumenDto obtenerResumenDeTienda(String idTienda, String tokenJwt) {
        try {
            // Endpoint que ms-inventario expone para dar el resumen de una tienda
            String url = urlMsInventario + "/api/inventario/resumen/tienda/" + idTienda;

            // Preparar la peticion con el token JWT
            HttpHeaders headers = new HttpHeaders();
            headers.set("Autorizacion", tokenJwt);

            HttpEntity<Void> peticion = new HttpEntity<>(headers);

            ResponseEntity<InventarioResumenDTO> respuesta = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    peticion,
                    InventarioResumenDTO.class
            );

            return respuesta.getBody();

        } catch (Exception e) {
            // Si ms-inventario no responde, retornar null y manejarlo en el servicio
            System.out.println("Error al consultar ms-inventario: " + e.getMessage());
            return null;
        }
    }
}
