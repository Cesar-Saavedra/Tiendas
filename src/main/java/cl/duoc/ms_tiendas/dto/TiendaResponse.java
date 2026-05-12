package cl.duoc.ms_tiendas.dto;

import cl.duoc.ms_tiendas.model.EstadoTienda;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TiendaResponse {

    private String id;
    private String nombre;
    private String descripcion;
    private String telefono;
    private String emailContacto;
    private String horarioAtencion;


    private EstadoTienda estado;


    private String idUsuarioCreador;


    private String nombreUsuarioCreador;


}
