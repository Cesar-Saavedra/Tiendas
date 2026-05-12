package cl.duoc.ms_tiendas.clientes;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import cl.duoc.ms_tiendas.dto.InventarioResumenDTO;
import lombok.RequiredArgsConstructor;
@Component
@RequiredArgsConstructor
public class InventarioCliente {

    
    private final RestTemplate restTemplate;

    // URL base de ms-inventario, configurada en application.properties
    @Value("${ms-inventario.url}")
    private String urlMsInventario;

    // ----------------------------------------------------------------
    // Consulta el resumen de inventario de una tienda
    // ----------------------------------------------------------------
    public InventarioResumenDTO obtenerResumenDeTienda(Long idTienda, String tokenJwt) {
        try {
            // Endpoint que ms-inventario expone para dar el resumen de una tienda
            String url = urlMsInventario + "/api/inventario/resumen/tienda/" + idTienda;

            // Preparar la peticion con el token JWT
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", tokenJwt);

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
