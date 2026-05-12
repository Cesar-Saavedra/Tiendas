package cl.duoc.ms_tiendas.dto;
import lombok.Data;

@Data
public class TiendaRequest {

        // Nombre comercial de la tienda
    private String nombre;

    // Descripcion de lo que ofrece la tienda
    private String descripcion;

    // Telefono de contacto
    private String telefono;

    // Email publico de la tienda
    private String emailContacto;

    // Horario en texto libre, ej: "Lun-Sab 10:00-21:00, Dom 12:00-18:00"
    private String horarioAtencion;
}
