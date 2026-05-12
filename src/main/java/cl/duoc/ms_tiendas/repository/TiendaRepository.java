package cl.duoc.ms_tiendas.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import cl.duoc.ms_tiendas.model.Tienda;

public interface TiendaRepository extends JpaRepository<Tienda, String> {


    List<Tienda> findByIdUsuarioCreador(String idUsuarioCreador);

    List<Tienda> findByEstado(String estado);

    Optional<Tienda> findByNombreandIdUsuarioCreador(String nombre, String idUsuarioCreador);



}
