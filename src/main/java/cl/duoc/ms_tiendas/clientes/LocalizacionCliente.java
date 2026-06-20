package cl.duoc.ms_tiendas.clientes;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

import cl.duoc.ms_tiendas.dto.LocalizacionDTO;

/*
 * Cliente Feign para comunicarse con ms-localizacion.
 *
 * name = "ms-localizacion" → Feign resuelve el host via Eureka,
 * sin URLs hardcodeadas en el yaml.
 */
@FeignClient(name = "ms-localizacion")
public interface LocalizacionCliente {

    /*
     * GET /api/localizacion/tienda/{id}
     * Devuelve la localizacion (direccion y coordenadas) de una tienda.
     *
     * @param idTienda  id de la tienda
     * @param tokenJwt  header "Bearer eyJ..." para autorizacion
     */
    @GetMapping("/api/localizacion/tienda/{id}")
    LocalizacionDTO obtenerLocalizacionDeTienda(
            @PathVariable("id") Integer idTienda,
            @RequestHeader("Authorization") String tokenJwt
    );
}
