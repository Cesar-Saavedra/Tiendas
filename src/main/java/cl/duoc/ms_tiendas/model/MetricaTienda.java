package cl.duoc.ms_tiendas.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "metricas_tienda")
public class MetricaTienda {

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private String id;

    @OneToOne
    @JoinColumn(name = "id_tienda", nullable=false)
    private Tienda tienda;

    @Column(nullable=false)
    private Integer totalVisitas = 0;

    @Column(nullable=false)
    private Integer totalFavoritos = 0;

    @Column(nullable=false)
    private Integer totalEventas = 0;

}
