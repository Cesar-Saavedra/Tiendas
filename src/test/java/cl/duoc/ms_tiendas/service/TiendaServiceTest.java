package cl.duoc.ms_tiendas.service;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import cl.duoc.ms_tiendas.dto.MetricaResponse;
import cl.duoc.ms_tiendas.dto.TiendaResumenDTO;
import cl.duoc.ms_tiendas.model.EstadoTienda;
import cl.duoc.ms_tiendas.model.MetricaTienda;
import cl.duoc.ms_tiendas.model.Tienda;
import cl.duoc.ms_tiendas.repository.MetricaTiendaRepositorio;
import cl.duoc.ms_tiendas.repository.TiendaRepositorio;

// NOTA: los metodos que dependen de LoginCliente (Feign) -crearTienda,
// listarTiendasActivas, obtenerPorId, actualizarTienda- quedan fuera del
// alcance de estas pruebas unitarias, por decision del equipo.
@ExtendWith(MockitoExtension.class)
public class TiendaServiceTest {

    @Mock
    private TiendaRepositorio tiendaRepositorio;

    @Mock
    private MetricaTiendaRepositorio metricaRepositorio;

    @InjectMocks  //injectamos el mock falso, para que no utilizar la clase real, sino el mock
    private TiendaServicio tiendaServicio;

    private Tienda tiendaEjemplo;

    @BeforeEach
    void setUp(){
        tiendaEjemplo = new Tienda();
        tiendaEjemplo.setId(1);
        tiendaEjemplo.setNombre("Carta Magica TCG");
        tiendaEjemplo.setHorarioAtencion("Lun-Sab 11:00-20:00");
        tiendaEjemplo.setEstado(EstadoTienda.ACTIVA);
        tiendaEjemplo.setIdUsuarioDueno(5);
    }

    // =====================================================================
    // obtenerResumenesPorDueno
    // =====================================================================

    @Test
    void obtenerResumenesPorDueno_retornaLista(){
        when(tiendaRepositorio.findByIdUsuarioDueno(5)).thenReturn(Arrays.asList(tiendaEjemplo));

        List<TiendaResumenDTO> resultado = tiendaServicio.obtenerResumenesPorDueno(5);

        assertEquals(1, resultado.size());
        assertEquals("Carta Magica TCG", resultado.get(0).getNombre());
    }

    @Test
    void obtenerResumenesPorDueno_sinTiendas_retornaListaVacia(){
        when(tiendaRepositorio.findByIdUsuarioDueno(99)).thenReturn(Arrays.asList());

        List<TiendaResumenDTO> resultado = tiendaServicio.obtenerResumenesPorDueno(99);

        assertEquals(0, resultado.size());
    }

    // =====================================================================
    // obtenerResumenParaOtrosMs
    // =====================================================================

    @Test
    void obtenerResumenParaOtrosMs_encontrada(){
        when(tiendaRepositorio.findById(1)).thenReturn(Optional.of(tiendaEjemplo));

        TiendaResumenDTO resumen = tiendaServicio.obtenerResumenParaOtrosMs(1);

        assertEquals(1, resumen.getId());
        assertEquals("ACTIVA", resumen.getEstado());
    }

    @Test
    void obtenerResumenParaOtrosMs_noEncontrada(){
        when(tiendaRepositorio.findById(99)).thenReturn(Optional.empty());

        RuntimeException error = assertThrows(RuntimeException.class, () ->
                tiendaServicio.obtenerResumenParaOtrosMs(99));

        assertEquals("No se encontro la tienda con id: 99", error.getMessage());
    }

    // =====================================================================
    // incrementarTotalEventos
    // =====================================================================

    @Test
    void incrementarTotalEventos_sumaUno(){
        MetricaTienda metricas = new MetricaTienda();
        metricas.setTienda(tiendaEjemplo);
        metricas.setTotalEventos(3);

        when(tiendaRepositorio.findById(1)).thenReturn(Optional.of(tiendaEjemplo));
        when(metricaRepositorio.findByTienda(tiendaEjemplo)).thenReturn(Optional.of(metricas));

        tiendaServicio.incrementarTotalEventos(1);

        assertEquals(4, metricas.getTotalEventos());
        verify(metricaRepositorio).save(metricas);
    }

    @Test
    void incrementarTotalEventos_tiendaNoEncontrada(){
        when(tiendaRepositorio.findById(99)).thenReturn(Optional.empty());

        RuntimeException error = assertThrows(RuntimeException.class, () ->
                tiendaServicio.incrementarTotalEventos(99));

        assertEquals("No se encontro la tienda con id: 99", error.getMessage());
    }

    @Test
    void incrementarTotalEventos_sinMetricas(){
        when(tiendaRepositorio.findById(1)).thenReturn(Optional.of(tiendaEjemplo));
        when(metricaRepositorio.findByTienda(tiendaEjemplo)).thenReturn(Optional.empty());

        RuntimeException error = assertThrows(RuntimeException.class, () ->
                tiendaServicio.incrementarTotalEventos(1));

        assertEquals("No se encontraron metricas para la tienda: 1", error.getMessage());
    }

    // =====================================================================
    // obtenerMetricas
    // =====================================================================

    @Test
    void obtenerMetricas_exitoso(){
        MetricaTienda metricas = new MetricaTienda();
        metricas.setTienda(tiendaEjemplo);
        metricas.setTotalVisitas(10);
        metricas.setTotalFavoritos(2);
        metricas.setTotalEventos(1);

        when(tiendaRepositorio.findById(1)).thenReturn(Optional.of(tiendaEjemplo));
        when(metricaRepositorio.findByTienda(tiendaEjemplo)).thenReturn(Optional.of(metricas));

        MetricaResponse respuesta = tiendaServicio.obtenerMetricas(1, 5);

        assertEquals(1, respuesta.getIdTienda());
        assertEquals(10, respuesta.getTotalVisitas());
    }

    @Test
    void obtenerMetricas_noEsDueno(){
        when(tiendaRepositorio.findById(1)).thenReturn(Optional.of(tiendaEjemplo));

        RuntimeException error = assertThrows(RuntimeException.class, () ->
                tiendaServicio.obtenerMetricas(1, 999));

        assertEquals("No tienes permiso para ver las metricas de esta tienda", error.getMessage());
    }

    @Test
    void obtenerMetricas_tiendaNoEncontrada(){
        when(tiendaRepositorio.findById(99)).thenReturn(Optional.empty());

        RuntimeException error = assertThrows(RuntimeException.class, () ->
                tiendaServicio.obtenerMetricas(99, 5));

        assertEquals("No se encontro la tienda con id: 99", error.getMessage());
    }

    @Test
    void obtenerMetricas_sinMetricas(){
        when(tiendaRepositorio.findById(1)).thenReturn(Optional.of(tiendaEjemplo));
        when(metricaRepositorio.findByTienda(tiendaEjemplo)).thenReturn(Optional.empty());

        RuntimeException error = assertThrows(RuntimeException.class, () ->
                tiendaServicio.obtenerMetricas(1, 5));

        assertEquals("No hay metricas para esta tienda aun", error.getMessage());
    }
}
