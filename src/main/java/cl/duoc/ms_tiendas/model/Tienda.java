package cl.duoc.ms_tiendas.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
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

    // Identificador unico autoincremental
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String id;

    // Nombre comercial de la tienda
    @Column(nullable = false, length = 150)
    private String nombre;

    // Descripcion breve de la tienda (que vende, su especialidad)
    @Column(length = 500)
    private String descripcion;

    // Telefono de contacto de la tienda
    @Column(length = 20)
    private String telefono;

    // Email de contacto publico de la tienda
    @Column(length = 150)
    private String emailContacto;

    // Horario de atencion en texto libre, ej: "Lun-Vie 10:00-20:00"
    @Column(length = 200)
    private String horarioAtencion;

    // Estado actual de la tienda en la plataforma
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoTienda estado = EstadoTienda.PENDIENTE;

    // ID del usuario dueno de la tienda (viene de ms-login)
    // No es una FK real porque esta en otro microservicio y otra BD
    @Column(nullable = false)
    private Long idUsuarioDueno; //Long aguanta que int o long

}