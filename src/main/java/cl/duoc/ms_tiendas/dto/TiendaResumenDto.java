package cl.duoc.ms_tiendas.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


// DTO resumido que se envia a otros microservicios cuando solo necesitan datos basicos
// Por ejemplo: ms-inventario pregunta "dame el nombre y estado de la tienda 3"
// Se evita enviar todos los campos para no saturar la red

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TiendaResumenDto {

    private String  id;
    private String nombre;
    private String horarioAtencion;
    private String estado;  
}
