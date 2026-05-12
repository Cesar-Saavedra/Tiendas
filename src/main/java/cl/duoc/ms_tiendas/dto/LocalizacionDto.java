package cl.duoc.ms_tiendas.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LocalizacionDto {

    private String id;
    private String idTienda;
    private String direccionCompleta;
    private Double latitud;
    private Double longitud;


}
