package cl.duoc.ms_tiendas.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import cl.duoc.ms_tiendas.model.MetricaTienda;
import cl.duoc.ms_tiendas.model.Tienda;

public interface MetricaTiendaRepository extends JpaRepository<MetricaTienda, String> {

    Optional<MetricaTienda> findByIdTienda(Tienda tienda);


}
