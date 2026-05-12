package cl.duoc.ms_tiendas.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;



// DTO que recibe los datos del formulario cuando se crea o actualiza una tienda
// El idUsuarioCreador llega desde el token JWT, no del body (se extrae en el controlador)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TiendaRequest {


    private String nombre;


    private String descripcion;


    private String telefono;


    private String emailContacto;

    
    private String horarioAtencion;
}
