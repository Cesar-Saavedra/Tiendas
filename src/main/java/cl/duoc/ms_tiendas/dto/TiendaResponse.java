package cl.duoc.ms_tiendas.dto;


import cl.duoc.ms_tiendas.model.EstadoTienda;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// DTO de respuesta con los datos completos de una tienda
// Se envia al cliente y a otros microservicios que consulten esta tienda
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TiendaResponse {

    private Long   id;
    private String nombre;
    private String descripcion;
    private String telefono;
    private String emailContacto;
    private String horarioAtencion;

    // Estado de la tienda: ACTIVA, INACTIVA o PENDIENTE
    private EstadoTienda estado;

    // ID del dueno (para que el frontend sepa si el usuario actual es dueno)
    private Long idUsuarioDueno;

    // Nombre del dueño enriquecido desde ms-login (puede ser null si ms-login no responde)
    private String nombreDueno;

}
