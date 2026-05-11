package cl.duoc.ms_tiendas.dto;

import lombok.Data;



// DTO que recibe los datos del formulario cuando se crea o actualiza una tienda
// El idUsuarioCreador llega desde el token JWT, no del body (se extrae en el controlador)
@Data
public class TiendaRequest {


    private String nombre;


    private String descripcion;


    private String telefono;


    private String emailContacto;

    
    private String horarioAtencion;
}
