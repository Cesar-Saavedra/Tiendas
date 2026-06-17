package cl.duoc.ms_tiendas.dto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class LocalizacionDTO {

        // ID de la localizacion en ms-localizacion
    private Integer id;

    // ID de la tienda a la que pertenece esta localizacion
    private Integer idTienda;

    // Direccion en texto legible para mostrar en la UI
    private String direccionCompleta;

    // Coordenadas para el mapa interactivo
    private Double latitud;
    private Double longitud;
}