package cl.duoc.ms_tiendas.dto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventarioResumenDTO {

    // ID de la tienda duena de este inventario
    private Integer idTienda;

    // Cantidad total de productos con stock mayor a 0
    private Integer totalProductosActivos;

    // Cantidad de categorias distintas que maneja la tienda
    private Integer totalCategorias;
}



// Copia local del resumen de inventario que viene de ms-inventario
// Solo trae el total de productos, no el detalle completo
// El detalle completo se consulta directamente a ms-inventario desde el frontend
