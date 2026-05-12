package cl.duoc.ms_tiendas.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioDTO {

    private String id;
    private String nombre;
    private String email;
    private String rol;
    private Boolean activo;

}
