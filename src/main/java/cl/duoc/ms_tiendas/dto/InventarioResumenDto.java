package cl.duoc.ms_tiendas.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventarioResumenDto {

    private String idTienda;
    private Integer totalProductosActivos;
    private Integer totalCategorias;

}
