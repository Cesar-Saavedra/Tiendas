package cl.duoc.ms_tiendas.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class TiendaRequest {

    // Nombre comercial de la tienda
    @NotBlank(message = "El nombre de la tienda es obligatorio")
    @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
    private String nombre;

    // Descripcion de lo que ofrece la tienda
    @Size(max = 500, message = "La descripcion no puede superar los 500 caracteres")
    private String descripcion;

    // Telefono de contacto
    private String telefono;

    // Email publico de la tienda
    @Email(message = "El email de contacto no tiene un formato valido")
    private String emailContacto;

    // Horario en texto libre, ej: "Lun-Sab 10:00-21:00, Dom 12:00-18:00"
    private String horarioAtencion;
}
