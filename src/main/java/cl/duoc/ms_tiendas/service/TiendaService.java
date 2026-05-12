package cl.duoc.ms_tiendas.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import cl.duoc.cliente.LoginCliente;
import cl.duoc.ms_tiendas.dto.TiendaRequest;
import cl.duoc.ms_tiendas.dto.TiendaResponse;
import cl.duoc.ms_tiendas.dto.UsuarioDto;
import cl.duoc.ms_tiendas.model.EstadoTienda;
import cl.duoc.ms_tiendas.model.MetricaTienda;
import cl.duoc.ms_tiendas.model.Tienda;
import cl.duoc.ms_tiendas.repository.MetricaTiendaRepository;
import cl.duoc.ms_tiendas.repository.TiendaRepository;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Service
@NoArgsConstructor  
@AllArgsConstructor
public class TiendaService {

    private  TiendaRepository tiendarepo;

    private  MetricaTiendaRepository metricatiendarepo;

    private  LoginCliente loginCliente;



    public TiendaResponse crearTienda(TiendaRequest solicitud, String idUsuarioCreador, String tokenJwt){

    Optional <Tienda> tiendaExistente = tiendarepo.findByNombreandIdUsuarioCreador(solicitud.getNombre(), idUsuarioCreador);       
    
    if (tiendaExistente.isPresent()) {
        throw new RuntimeException("Ya existe una tienda con el nombre" + solicitud.getNombre());
    }

    Tienda nuevaTienda = new Tienda();
    nuevaTienda.setNombre(solicitud.getNombre());
    nuevaTienda.setDescripcion(solicitud.getDescripcion());
    nuevaTienda.setTelefono(solicitud.getTelefono());
    nuevaTienda.setEmailContacto(solicitud.getEmailContacto());
    nuevaTienda.setHorarioAtencion(solicitud.getHorarioAtencion());
    nuevaTienda.setIdUsuarioCreador(idUsuarioCreador);
    nuevaTienda.setEstado(EstadoTienda.PENDIENTE);


    Tienda tiendaGuardada = tiendarepo.save(nuevaTienda);


    MetricaTienda metrica = new MetricaTienda();
    metrica.setTienda(tiendaGuardada);
    metrica.setTotalVisitas(0);
    metrica.setTotalFavoritos(0);
    metrica.setTotalEventos(0);
    metricatiendarepo.save(metrica);


    UsuarioDto creador = loginCliente.obtenerUsuarioPorId(idUsuarioCreador, tokenJwt);

    return convertirARespuesta(tiendaGuardada, creador);
    }

    public List<TiendaResponse> listarTiendasActivas (String tokenJwt){
        
        List<Tienda> tiendasActivas = tiendarepo.findByEstado(EstadoTienda.ACTIVA);
        
        List<TiendaResponse> listarespuestas = new ArrayList<>();

        for (Tienda tienda : tiendasActivas) {
            UsuarioDto creador = loginCliente.obtenerUsuarioPorId(tienda.getIdUsuarioCreador(), tokenJwt);
            listarespuestas.add(convertirARespuesta(tienda, creador));
        }

        return listarespuestas;


    // ----------------------------------------------------------------
    // Obtiene el perfil completo de una tienda por su ID
    // Tambien suma una visita a las metricas
    // ----------------------------------------------------------------
    public TiendaResponse obtenerPorId(String idTienda, String tokenJwt){

        Tienda tienda = tiendarepo.findById(idTienda)
            .orElseThrow(() -> new RuntimeException("Tienda no encontrada con id: " + idTienda));

        UsuarioDto creador = loginCliente.obtenerUsuarioPorId(tienda.getIdUsuarioCreador(), tokenJwt);

        return convertirARespuesta(tienda, creador);
    }


// ----------------------------------------------------------------
// Actualiza los datos de una tienda (solo el creador puede hacerlo)
// ----------------------------------------------------------------
    public TiendaResponse actualizarTienda(Long idTienda, TiendaRequest solicitud, Long idUsuariocreador, String tokenJwt) {

        // Buscar la tienda en la BD
        Tienda tienda = tiendarepo.findById(idTienda)
                .orElseThrow(() -> new RuntimeException("No se encontro la tienda con id: " + idTienda));

        // Verificar que quien edita sea realmente el creador
        if (!tienda.getIdUsuarioCreador().equals(idUsuarioCreador)) {
            throw new RuntimeException("No tienes permiso para editar esta tienda");
        }

        // Actualizar los campos con los nuevos datos
        tienda.setNombre(solicitud.getNombre());
        tienda.setDescripcion(solicitud.getDescripcion());
        tienda.setTelefono(solicitud.getTelefono());
        tienda.setEmailContacto(solicitud.getEmailContacto());
        tienda.setHorarioAtencion(solicitud.getHorarioAtencion());

        // Guardar los cambios en la BD
        Tienda tiendaActualizada = tiendarepo.save(tienda);

        // Consultar nombre del creador en ms-login
        UsuarioDto creador = loginCliente.obtenerUsuarioPorId(idUsuarioCreador, tokenJwt);

        return convertirARespuesta(tiendaActualizada, creador);
    
    }


    // ----------------------------------------------------------------
    // Endpoint para otros microservicios: devuelve resumen de una tienda
    // Lo usan: ms-inventario, ms-localizacion, ms-eventos
    // ----------------------------------------------------------------
    public TiendaResumenDTO obtenerResumenParaOtrosMs(Long idTienda) {

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
    public void incrementarTotalEventos(Long idTienda) {

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
    // Devuelve las metricas de una tienda (solo para el creador)
    // ----------------------------------------------------------------
    public MetricaResponse obtenerMetricas(Long idTienda, Long idUsuariocreador) {

        // Buscar la tienda y verificar que el usuario sea el creador
        Tienda tienda = tiendaRepositorio.findById(idTienda)
                .orElseThrow(() -> new RuntimeException("No se encontro la tienda con id: " + idTienda));

        if (!tienda.getIdUsuarioCreador().equals(idUsuariocreador)) {
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
    // El parametro creador puede ser null si ms-login no respondio
    private TiendaResponse convertirARespuesta(Tienda tienda, UsuarioDTO creador) {
        TiendaResponse respuesta = new TiendaResponse();
        respuesta.setId(tienda.getId());
        respuesta.setNombre(tienda.getNombre());
        respuesta.setDescripcion(tienda.getDescripcion());
        respuesta.setTelefono(tienda.getTelefono());
        respuesta.setEmailContacto(tienda.getEmailContacto());
        respuesta.setHorarioAtencion(tienda.getHorarioAtencion());
        respuesta.setEstado(tienda.getEstado());
        respuesta.setIdUsuariocreador(tienda.getIdUsuarioCreador());

        // Si ms-login respondio, agregamos el nombre; si no, dejamos "Desconocido"
        respuesta.setNombrecreador(creador != null ? creador.getNombre() : "Desconocido");

        return respuesta;
    }


}
