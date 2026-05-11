package cl.duoc.ms_tiendas.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MetricaResponse {


    private String idTienda;
    private String nombreTienda;


    private Integer totalVisitas;
    private Integer TotalFavoritos;
    private Integer totalEventos;







}
