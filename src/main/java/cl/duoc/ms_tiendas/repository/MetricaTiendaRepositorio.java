package cl.duoc.ms_tiendas.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import cl.duoc.ms_tiendas.model.MetricaTienda;
import cl.duoc.ms_tiendas.model.Tienda;


public interface MetricaTiendaRepositorio extends JpaRepository<MetricaTienda, Integer> {

    Optional<MetricaTienda> findByTienda(Tienda tienda);

}