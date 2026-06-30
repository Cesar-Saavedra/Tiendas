package cl.duoc.ms_tiendas.controller;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import cl.duoc.ms_tiendas.dto.MetricaResponse;
import cl.duoc.ms_tiendas.dto.TiendaRequest;
import cl.duoc.ms_tiendas.dto.TiendaResponse;
import cl.duoc.ms_tiendas.dto.TiendaResumenDTO;
import cl.duoc.ms_tiendas.model.EstadoTienda;
import cl.duoc.ms_tiendas.service.TiendaServicio;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(TiendaControlador.class)
@AutoConfigureMockMvc(addFilters = false)
public class TiendaControladorTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TiendaServicio tiendaServicio;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private TiendaResponse tiendaResponseEjemplo;

    @BeforeEach
    void setUp(){
        tiendaResponseEjemplo = new TiendaResponse();
        tiendaResponseEjemplo.setId(1);
        tiendaResponseEjemplo.setNombre("Carta Magica TCG");
        tiendaResponseEjemplo.setEstado(EstadoTienda.ACTIVA);
        tiendaResponseEjemplo.setIdUsuarioDueno(5);
        tiendaResponseEjemplo.setNombreDueno("Tomas");
    }

    // =====================================================================
    // GET /api/tiendas
    // =====================================================================

    @Test
    void listarTiendas_retorna200() throws Exception {
        when(tiendaServicio.listarTiendasActivas(anyString())).thenReturn(Arrays.asList(tiendaResponseEjemplo));

        mockMvc.perform(get("/api/tiendas").header("Authorization", "Bearer token-fake"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("Carta Magica TCG"));
    }

    // =====================================================================
    // GET /api/tiendas/{id}
    // =====================================================================

    @Test
    void obtenerTienda_retorna200() throws Exception {
        when(tiendaServicio.obtenerPorId(1, "Bearer token-fake")).thenReturn(tiendaResponseEjemplo);

        mockMvc.perform(get("/api/tiendas/1").header("Authorization", "Bearer token-fake"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void obtenerTienda_noEncontrada_propagaError() {
        when(tiendaServicio.obtenerPorId(99, "Bearer token-fake"))
                .thenThrow(new RuntimeException("No se encontro la tienda con id: 99"));

        // El controlador no captura la excepcion: se propaga tal cual hasta el servlet.
        Exception excepcion = org.junit.jupiter.api.Assertions.assertThrows(Exception.class, () ->
                mockMvc.perform(get("/api/tiendas/99").header("Authorization", "Bearer token-fake")));

        org.junit.jupiter.api.Assertions.assertTrue(excepcion.getMessage().contains("No se encontro la tienda con id: 99"));
    }

    // =====================================================================
    // POST /api/tiendas
    // =====================================================================

    @Test
    void crearTienda_retorna201() throws Exception {
        TiendaRequest request = new TiendaRequest();
        request.setNombre("Carta Magica TCG");
        request.setDescripcion("Especialistas en TCG");

        when(tiendaServicio.crearTienda(any(TiendaRequest.class), eq(5), anyString())).thenReturn(tiendaResponseEjemplo);

        mockMvc.perform(post("/api/tiendas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer token-fake")
                        .requestAttr("X-Usuario-Id", 5)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }

    // =====================================================================
    // PUT /api/tiendas/{id}
    // =====================================================================

    @Test
    void actualizarTienda_retorna200() throws Exception {
        TiendaRequest request = new TiendaRequest();
        request.setNombre("Carta Magica TCG Editada");

        when(tiendaServicio.actualizarTienda(eq(1), any(TiendaRequest.class), eq(5), anyString()))
                .thenReturn(tiendaResponseEjemplo);

        mockMvc.perform(put("/api/tiendas/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer token-fake")
                        .requestAttr("X-Usuario-Id", 5)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    // =====================================================================
    // GET /api/tiendas/{id}/resumen
    // =====================================================================

    @Test
    void obtenerResumen_retorna200() throws Exception {
        TiendaResumenDTO resumen = new TiendaResumenDTO(1, "Carta Magica TCG", "Lun-Sab 11:00-20:00", "ACTIVA");
        when(tiendaServicio.obtenerResumenParaOtrosMs(1)).thenReturn(resumen);

        mockMvc.perform(get("/api/tiendas/1/resumen"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("ACTIVA"));
    }

    // =====================================================================
    // GET /api/tiendas/dueno/{usuarioId}
    // =====================================================================

    @Test
    void obtenerTiendasPorDueno_retorna200() throws Exception {
        TiendaResumenDTO resumen = new TiendaResumenDTO(1, "Carta Magica TCG", "Lun-Sab 11:00-20:00", "ACTIVA");
        when(tiendaServicio.obtenerResumenesPorDueno(5)).thenReturn(Arrays.asList(resumen));

        mockMvc.perform(get("/api/tiendas/dueno/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    // =====================================================================
    // PUT /api/tiendas/{id}/sumar-evento
    // =====================================================================

    @Test
    void sumarEvento_retorna204() throws Exception {
        mockMvc.perform(put("/api/tiendas/1/sumar-evento"))
                .andExpect(status().isNoContent());

        verify(tiendaServicio).incrementarTotalEventos(1);
    }

    // =====================================================================
    // GET /api/tiendas/{id}/metricas
    // =====================================================================

    @Test
    void obtenerMetricas_retorna200() throws Exception {
        MetricaResponse metricas = new MetricaResponse(1, "Carta Magica TCG", 10, 2, 1);
        when(tiendaServicio.obtenerMetricas(1, 5)).thenReturn(metricas);

        mockMvc.perform(get("/api/tiendas/1/metricas").requestAttr("X-Usuario-Id", 5))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalVisitas").value(10));
    }
}
