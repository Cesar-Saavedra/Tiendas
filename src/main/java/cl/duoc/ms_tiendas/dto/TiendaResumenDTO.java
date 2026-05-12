package cl.duoc.ms_tiendas.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TiendaResumenDTO {

    private Integer id;
    private String nombre;
    private String horarioAtencion;
    private String estado;
}

