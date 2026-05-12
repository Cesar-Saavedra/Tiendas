package cl.duoc.ms_tiendas.dto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor; 

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MetricaResponse {
    
    // ID de la tienda a la que pertenecen estas metricas
    private Integer idTienda;
    private String nombreTienda;

    // Metricas principales
    private Integer totalVisitas;
    private Integer totalFavoritos;
    private Integer totalEventos;
}

