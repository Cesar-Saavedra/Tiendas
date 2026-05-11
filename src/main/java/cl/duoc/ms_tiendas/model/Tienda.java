package cl.duoc.ms_tiendas.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "tiendas")
public class Tienda {

    @Id
    @GeneratedValue
    private String id;

    @Column(nullable = false)
    private String nombre;

    @Column(length = 500)
    private String descripcion;

    @Column(length = 20)
    private String telefono;

    @Column(length = 150)
    private String emailContacto;

    @Column(length = 200)
    private String horarioAtencion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoTienda estado = EstadoTienda.PENDIENTE;


    @Column(nullable=false)
    private String idUsuarioCreador;












































}
