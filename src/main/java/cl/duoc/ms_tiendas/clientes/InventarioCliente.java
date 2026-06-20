package cl.duoc.ms_tiendas.clientes;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

import cl.duoc.ms_tiendas.dto.InventarioResumenDTO;

/*
 * Cliente Feign para comunicarse con ms-inventario.
 *
 * name = "ms-inventario" → Feign resuelve el host via Eureka,
 * sin URLs hardcodeadas en el yaml.
 */
@FeignClient(name = "ms-inventario")
public interface InventarioCliente {

    /*
     * GET /api/inventario/resumen/tienda/{id}
     * Devuelve el resumen de inventario de una tienda (total productos, categorias).
     *
     * @param idTienda  id de la tienda
     * @param tokenJwt  header "Bearer eyJ..." para autorizacion
     */
    @GetMapping("/api/inventario/resumen/tienda/{id}")
    InventarioResumenDTO obtenerResumenDeTienda(
            @PathVariable("id") Integer idTienda,
            @RequestHeader("Authorization") String tokenJwt
    );
}
