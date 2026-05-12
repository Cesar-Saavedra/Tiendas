package cl.duoc.ms_tiendas.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import cl.duoc.ms_tiendas.model.EstadoTienda;
import cl.duoc.ms_tiendas.model.Tienda;

// Repositorio que maneja las consultas a la tabla "tiendas"
// JpaRepository nos da gratis: save, findById, findAll, delete, etc.
public interface TiendaRepositorio extends JpaRepository<Tienda, String> {

    List<Tienda> findByIdUsuarioDueno(Long idUsuarioDueno);


    List<Tienda> findByEstado(EstadoTienda estado);


    Optional<Tienda> findByNombreAndIdUsuarioDueno(String nombre, Long idUsuarioDueno);

}
