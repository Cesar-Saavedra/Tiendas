package cl.duoc.ms_tiendas.clientes;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

import cl.duoc.ms_tiendas.dto.UsuarioDTO;

/*
 * Cliente Feign para comunicarse con ms-login.
 *
 * name = "ms-login" → Feign resuelve el host via Eureka (lb://ms-login),
 * sin URLs hardcodeadas en el yaml.
 *
 * Cada metodo mapea 1:1 con un endpoint real de ms-login.
 */
@FeignClient(name = "ms-login")
public interface LoginCliente {

    /*
     * GET /api/usuarios/{id}
     * Devuelve los datos del usuario (nombre, email, rol).
     * Se usa para enriquecer TiendaResponse con el nombre del dueno.
     *
     * @param idUsuario  id del usuario en ms-login
     * @param tokenJwt   header "Bearer eyJ..." para que ms-login autorice la peticion
     */
    @GetMapping("/api/usuarios/{id}")
    UsuarioDTO obtenerUsuarioPorId(
            @PathVariable("id") Integer idUsuario,
            @RequestHeader("Authorization") String tokenJwt
    );
}
