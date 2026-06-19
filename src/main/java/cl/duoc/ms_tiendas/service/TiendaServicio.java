package cl.duoc.ms_tiendas.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cl.duoc.ms_tiendas.clientes.LoginCliente;
import cl.duoc.ms_tiendas.dto.MetricaResponse;
import cl.duoc.ms_tiendas.dto.TiendaRequest;
import cl.duoc.ms_tiendas.dto.TiendaResponse;
import cl.duoc.ms_tiendas.dto.TiendaResumenDTO;
import cl.duoc.ms_tiendas.dto.UsuarioDTO;
import cl.duoc.ms_tiendas.model.EstadoTienda;
import cl.duoc.ms_tiendas.model.MetricaTienda;
import cl.duoc.ms_tiendas.model.Tienda;
import cl.duoc.ms_tiendas.repository.MetricaTiendaRepositorio;
import cl.duoc.ms_tiendas.repository.TiendaRepositorio;

@Service
public class TiendaServicio {

    // Repositorio para acceder a la tabla de tiendas
    @Autowired
    private TiendaRepositorio tiendaRepositorio;

    // Repositorio para acceder a la tabla de metricas
    @Autowired
    private MetricaTiendaRepositorio metricaRepositorio;

    // Cliente HTTP para consultar datos de usuarios en ms-login
    @Autowired
    private LoginCliente loginCliente;

    // ----------------------------------------------------------------
    // Crea una nueva tienda para un usuario autenticado
    // El idUsuarioDueno viene del token JWT, no del body del formulario
    // ----------------------------------------------------------------

    public TiendaResponse crearTienda(TiendaRequest solicitud, Integer idUsuarioDueno, String tokenJwt) {

        // Verificar que el usuario no tenga ya una tienda con el mismo nombre
        Optional<Tienda> tiendaExistente = tiendaRepositorio.findByNombreAndIdUsuarioDueno(
                solicitud.getNombre(), idUsuarioDueno
        );
        if (tiendaExistente.isPresent()) {
            throw new RuntimeException("Ya tienes una tienda con el nombre: " + solicitud.getNombre());
        }

        // Crear la entidad Tienda con los datos del formulario
        Tienda nuevaTienda = new Tienda();
        nuevaTienda.setNombre(solicitud.getNombre());
        nuevaTienda.setDescripcion(solicitud.getDescripcion());
        nuevaTienda.setTelefono(solicitud.getTelefono());
        nuevaTienda.setEmailContacto(solicitud.getEmailContacto());
        nuevaTienda.setHorarioAtencion(solicitud.getHorarioAtencion());
        nuevaTienda.setEstado(EstadoTienda.PENDIENTE); // toda tienda empieza en PENDIENTE
        nuevaTienda.setIdUsuarioDueno(idUsuarioDueno);

        // Guardar la tienda en la base de datos
        Tienda tiendaGuardada = tiendaRepositorio.save(nuevaTienda);

        // Crear automaticamente las metricas en cero para esta tienda nueva
        MetricaTienda metricas = new MetricaTienda();
        metricas.setTienda(tiendaGuardada);
        metricas.setTotalVisitas(0);
        metricas.setTotalFavoritos(0);
        metricas.setTotalEventos(0);
        metricaRepositorio.save(metricas);

        // Consultar el nombre del dueño en ms-login para enriquecer la respuesta
        // Si ms-login no responde, igual devolvemos la tienda sin el nombre del dueño
        UsuarioDTO dueno = loginCliente.obtenerUsuarioPorId(idUsuarioDueno, tokenJwt);

        return convertirARespuesta(tiendaGuardada, dueno);
    }

    // ----------------------------------------------------------------
    // Lista todas las tiendas activas (vista publica para los jugadores)
    // ----------------------------------------------------------------
    public List<TiendaResponse> listarTiendasActivas(String tokenJwt) {

        // Obtener solo las tiendas con estado ACTIVA
        List<Tienda> tiendasActivas = tiendaRepositorio.findByEstado(EstadoTienda.ACTIVA);

        // Convertir cada tienda a DTO de respuesta
        List<TiendaResponse> listaRespuesta = new ArrayList<>();
        for (Tienda tienda : tiendasActivas) {

            // Consultar el nombre del dueno en ms-login
            UsuarioDTO dueno = loginCliente.obtenerUsuarioPorId(tienda.getIdUsuarioDueno(), tokenJwt);
            listaRespuesta.add(convertirARespuesta(tienda, dueno));
        }

        return listaRespuesta;
    }

    // ----------------------------------------------------------------
    // Obtiene el perfil completo de una tienda por su ID
    // Tambien suma una visita a las metricas
    // ----------------------------------------------------------------
    public TiendaResponse obtenerPorId(Integer idTienda, String tokenJwt) {

        // Buscar la tienda, lanzar error si no existe
        Tienda tienda = tiendaRepositorio.findById(idTienda)
            .orElseThrow(() -> new RuntimeException("No se encontro la tienda con id: " + idTienda));

        // Registrar una visita en las metricas de esta tienda
        registrarVisita(tienda);

        // Consultar nombre del dueno en ms-login
        UsuarioDTO dueno = loginCliente.obtenerUsuarioPorId(tienda.getIdUsuarioDueno(), tokenJwt);

        return convertirARespuesta(tienda, dueno);
    }

    // ----------------------------------------------------------------
    // Actualiza los datos de una tienda (solo el dueno puede hacerlo)
    // ----------------------------------------------------------------
    public TiendaResponse actualizarTienda(Integer idTienda, TiendaRequest solicitud, Integer idUsuarioDueno, String tokenJwt) {

        // Buscar la tienda en la BD
        Tienda tienda = tiendaRepositorio.findById(idTienda)
                .orElseThrow(() -> new RuntimeException("No se encontro la tienda con id: " + idTienda));

        // Verificar que quien edita sea realmente el dueno
        if (!tienda.getIdUsuarioDueno().equals(idUsuarioDueno)) {
            throw new RuntimeException("No tienes permiso para editar esta tienda");
        }

        // Actualizar los campos con los nuevos datos
        tienda.setNombre(solicitud.getNombre());
        tienda.setDescripcion(solicitud.getDescripcion());
        tienda.setTelefono(solicitud.getTelefono());
        tienda.setEmailContacto(solicitud.getEmailContacto());
        tienda.setHorarioAtencion(solicitud.getHorarioAtencion());

        // Guardar los cambios en la BD
        Tienda tiendaActualizada = tiendaRepositorio.save(tienda);

        // Consultar nombre del dueno en ms-login
        UsuarioDTO dueno = loginCliente.obtenerUsuarioPorId(idUsuarioDueno, tokenJwt);

        return convertirARespuesta(tiendaActualizada, dueno);
    }

    // ----------------------------------------------------------------
    // Endpoint para otros microservicios: devuelve las tiendas que pertenecen
    // a un usuario (por su id en ms-login). Lo usa ms-eventos para verificar
    // si la tienda organizadora de un evento es realmente del usuario autenticado,
    // ya que el id de la tienda es distinto del id del usuario dueno.
    // ----------------------------------------------------------------
    public List<TiendaResumenDTO> obtenerResumenesPorDueno(Integer idUsuarioDueno) {
        List<Tienda> tiendas = tiendaRepositorio.findByIdUsuarioDueno(idUsuarioDueno);

        List<TiendaResumenDTO> resumenes = new ArrayList<>();
        for (Tienda tienda : tiendas) {
            resumenes.add(new TiendaResumenDTO(
                    tienda.getId(),
                    tienda.getNombre(),
                    tienda.getHorarioAtencion(),
                    tienda.getEstado().name()
            ));
        }
        return resumenes;
    }

    // ----------------------------------------------------------------
    // Endpoint para otros microservicios: devuelve resumen de una tienda
    // Lo usan: ms-inventario, ms-localizacion, ms-eventos
    // ----------------------------------------------------------------
    public TiendaResumenDTO obtenerResumenParaOtrosMs(Integer idTienda) {

        // Buscar la tienda en la BD
        Tienda tienda = tiendaRepositorio.findById(idTienda)
            .orElseThrow(() -> new RuntimeException("No se encontro la tienda con id: " + idTienda));

        // Convertir a resumen (sin datos sensibles ni llamadas a otros MS)
        TiendaResumenDTO resumen = new TiendaResumenDTO();
        resumen.setId(tienda.getId());
        resumen.setNombre(tienda.getNombre());
        resumen.setHorarioAtencion(tienda.getHorarioAtencion());
        resumen.setEstado(tienda.getEstado().name());

        return resumen;
    }

    // ----------------------------------------------------------------
    // Incrementa el contador de eventos cuando ms-eventos crea uno nuevo
    // ms-eventos llama a este endpoint cuando una tienda crea un torneo
    // ----------------------------------------------------------------
    public void incrementarTotalEventos(Integer idTienda) {

        // Buscar la tienda
        Tienda tienda = tiendaRepositorio.findById(idTienda)
                .orElseThrow(() -> new RuntimeException("No se encontro la tienda con id: " + idTienda));

        // Buscar sus metricas
        MetricaTienda metricas = metricaRepositorio.findByTienda(tienda)
                .orElseThrow(() -> new RuntimeException("No se encontraron metricas para la tienda: " + idTienda));

        // Sumar 1 al contador de eventos y guardar
        metricas.setTotalEventos(metricas.getTotalEventos() + 1);
        metricaRepositorio.save(metricas);
    }

    // ----------------------------------------------------------------
    // Devuelve las metricas de una tienda (solo para el dueno)
    // ----------------------------------------------------------------
    public MetricaResponse obtenerMetricas(Integer idTienda, Integer idUsuarioDueno) {

        // Buscar la tienda y verificar que el usuario sea el dueno
        Tienda tienda = tiendaRepositorio.findById(idTienda)
                .orElseThrow(() -> new RuntimeException("No se encontro la tienda con id: " + idTienda));

        if (!tienda.getIdUsuarioDueno().equals(idUsuarioDueno)) {
            throw new RuntimeException("No tienes permiso para ver las metricas de esta tienda");
        }

        // Buscar las metricas asociadas
        MetricaTienda metricas = metricaRepositorio.findByTienda(tienda)
                .orElseThrow(() -> new RuntimeException("No hay metricas para esta tienda aun"));

        // Armar y retornar la respuesta
        MetricaResponse respuesta = new MetricaResponse();
        respuesta.setIdTienda(tienda.getId());
        respuesta.setNombreTienda(tienda.getNombre());
        respuesta.setTotalVisitas(metricas.getTotalVisitas());
        respuesta.setTotalFavoritos(metricas.getTotalFavoritos());
        respuesta.setTotalEventos(metricas.getTotalEventos());

        return respuesta;
    }

    // ----------------------------------------------------------------
    // Metodos privados de apoyo
    // ----------------------------------------------------------------

    // Suma una visita al contador de metricas de la tienda
    private void registrarVisita(Tienda tienda) {
        Optional<MetricaTienda> metricas = metricaRepositorio.findByTienda(tienda);
        if (metricas.isPresent()) {
            metricas.get().setTotalVisitas(metricas.get().getTotalVisitas() + 1);
            metricaRepositorio.save(metricas.get());
        }
    }

    // Convierte una entidad Tienda en el DTO de respuesta
    // El parametro dueno puede ser null si ms-login no respondio
    private TiendaResponse convertirARespuesta(Tienda tienda, UsuarioDTO dueno) {
        TiendaResponse respuesta = new TiendaResponse();
        respuesta.setId(tienda.getId());
        respuesta.setNombre(tienda.getNombre());
        respuesta.setDescripcion(tienda.getDescripcion());
        respuesta.setTelefono(tienda.getTelefono());
        respuesta.setEmailContacto(tienda.getEmailContacto());
        respuesta.setHorarioAtencion(tienda.getHorarioAtencion());
        respuesta.setEstado(tienda.getEstado());
        respuesta.setIdUsuarioDueno(tienda.getIdUsuarioDueno());

        // Si ms-login respondio, agregamos el nombre; si no, dejamos "Desconocido"
        respuesta.setNombreDueno(dueno != null ? dueno.getNombre() : "Desconocido");

        return respuesta;
    }
}