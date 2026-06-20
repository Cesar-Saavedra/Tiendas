package cl.duoc.ms_tiendas.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cl.duoc.ms_tiendas.dto.MetricaResponse;
import cl.duoc.ms_tiendas.dto.TiendaRequest;
import cl.duoc.ms_tiendas.dto.TiendaResponse;
import cl.duoc.ms_tiendas.dto.TiendaResumenDTO;
import org.springframework.beans.factory.annotation.Autowired;
import cl.duoc.ms_tiendas.service.TiendaServicio;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/tiendas")
@Tag(name = "Tiendas", description = "Gestión de tiendas de TCG")
public class TiendaControlador {

    // El controlador solo conoce al servicio, nunca al repositorio directamente
    @Autowired
    private TiendaServicio tiendaServicio;

    // ================================================================
    // ENDPOINTS PUBLICOS (cualquier usuario autenticado puede usarlos)
    // Header requerido: Authorization: Bearer <token>
    // ================================================================

    /*
     * GET /api/tiendas
     * Lista todas las tiendas con estado ACTIVA
     * Lo usan: los jugadores para buscar tiendas cercanas
     *
     * Respuesta exitosa (200): lista de TiendaResponse
     */
    @GetMapping
    @Operation(summary = "Listar tiendas activas", description = "Devuelve todas las tiendas con estado ACTIVA.")
    public ResponseEntity<List<TiendaResponse>> listarTiendas(
            @RequestHeader("Authorization") String tokenJwt) {

        List<TiendaResponse> tiendas = tiendaServicio.listarTiendasActivas(tokenJwt);
        return ResponseEntity.ok(tiendas);
    }

    /*
     * GET /api/tiendas/{id}
     * Obtiene el perfil completo de una tienda y suma una visita a sus metricas
     *
     * Ejemplo: GET /api/tiendas/1
     */
    @GetMapping("/{id}")
    @Operation(summary = "Ver tienda por ID", description = "Obtiene el perfil completo de una tienda y suma una visita a sus métricas.")
    public ResponseEntity<TiendaResponse> obtenerTienda(
            @PathVariable Integer id,
            @RequestHeader("Authorization") String tokenJwt) {

        TiendaResponse tienda = tiendaServicio.obtenerPorId(id, tokenJwt);
        return ResponseEntity.ok(tienda);
    }

    /*
     * POST /api/tiendas
     * Crea una nueva tienda para el usuario autenticado
     * Solo usuarios con rol TIENDA deberian poder hacer esto
     *
     * Header: Authorization: Bearer <token>
     * Header: X-Usuario-Id: 5   ← lo agrega el FiltroJwt automaticamente
     *
     * Body JSON de ejemplo:
     * {
     *   "nombre": "Carta Magica TCG",
     *   "descripcion": "Especialistas en Magic the Gathering y Pokemon",
     *   "telefono": "+56912345678",
     *   "emailContacto": "contacto@cartamagica.cl",
     *   "horarioAtencion": "Lun-Sab 11:00-20:00"
     * }
     */
    @PostMapping
    @Operation(summary = "Crear tienda", description = "Crea una nueva tienda para el usuario autenticado (rol TIENDA requerido).")
    public ResponseEntity<TiendaResponse> crearTienda(
            @Valid @RequestBody TiendaRequest solicitud,
            @RequestHeader("Authorization") String tokenJwt,
            @RequestAttribute("X-Usuario-Id") Integer idUsuarioDueno) {

        TiendaResponse tiendaCreada = tiendaServicio.crearTienda(solicitud, idUsuarioDueno, tokenJwt);
        // 201 Created en lugar de 200 OK porque estamos creando un recurso nuevo
        return ResponseEntity.status(HttpStatus.CREATED).body(tiendaCreada);
    }

    /*
     * PUT /api/tiendas/{id}
     * Actualiza los datos de una tienda
     * Solo el dueno de la tienda puede actualizarla
     *
     * Body JSON: mismos campos que el POST
     */
    @PutMapping("/{id}")
    @Operation(summary = "Actualizar tienda", description = "Actualiza los datos de la tienda. Solo el dueño puede hacerlo.")
    public ResponseEntity<TiendaResponse> actualizarTienda(
            @PathVariable Integer id,
            @Valid @RequestBody TiendaRequest solicitud,
            @RequestHeader("Authorization") String tokenJwt,
            @RequestAttribute("X-Usuario-Id") Integer idUsuarioDueno) {

        TiendaResponse tiendaActualizada = tiendaServicio.actualizarTienda(id, solicitud, idUsuarioDueno, tokenJwt);
        return ResponseEntity.ok(tiendaActualizada);
    }

    // ================================================================
    // ENDPOINTS INTERNOS (solo para otros microservicios de CardLink)
    // Header requerido: Authorization: Bearer <token>
    // ================================================================

    /*
     * GET /api/tiendas/{id}/resumen
     * Devuelve solo los datos basicos de una tienda
     * Usado por: ms-inventario, ms-localizacion, ms-eventos
     *
     * Respuesta:
     * {
     *   "id": 1,
     *   "nombre": "Carta Magica TCG",
     *   "horarioAtencion": "Lun-Sab 11:00-20:00",
     *   "estado": "ACTIVA"
     * }
     */
    @GetMapping("/{id}/resumen")
    @Operation(summary = "Resumen de tienda (interno)", description = "Devuelve datos básicos de la tienda. Usado por ms-inventario, ms-localizacion y ms-eventos via Feign.")
    public ResponseEntity<TiendaResumenDTO> obtenerResumen(@PathVariable Integer id) {
        TiendaResumenDTO resumen = tiendaServicio.obtenerResumenParaOtrosMs(id);
        return ResponseEntity.ok(resumen);
    }

    /*
     * GET /api/tiendas/dueno/{usuarioId}
     * Devuelve las tiendas que pertenecen a un usuario (id de ms-login).
     * Lo usa ms-eventos para verificar si la tienda organizadora de un
     * evento es realmente del usuario autenticado: el id de la tienda
     * NO es el mismo que el id del usuario dueno.
     */
    @GetMapping("/dueno/{usuarioId}")
    @Operation(summary = "Tiendas por dueño (interno)", description = "Devuelve las tiendas que pertenecen a un usuario. Usado por ms-eventos via Feign.")
    public ResponseEntity<List<TiendaResumenDTO>> obtenerTiendasPorDueno(@PathVariable Integer usuarioId) {
        List<TiendaResumenDTO> tiendas = tiendaServicio.obtenerResumenesPorDueno(usuarioId);
        return ResponseEntity.ok(tiendas);
    }

    /*
     * PUT /api/tiendas/{id}/sumar-evento
     * Incrementa el contador de eventos de una tienda
     * Llamado por: ms-eventos cada vez que una tienda crea un torneo o evento
     *
     * Ejemplo: PUT /api/tiendas/1/sumar-evento
     * Body: vacio
     */
    @PutMapping("/{id}/sumar-evento")
    @Operation(summary = "Sumar evento a tienda (interno)", description = "Incrementa el contador de eventos de la tienda. Llamado por ms-eventos via Feign.")
    public ResponseEntity<Void> sumarEvento(@PathVariable Integer id) {
        tiendaServicio.incrementarTotalEventos(id);
        // 204 No Content: operacion exitosa pero sin cuerpo de respuesta
        return ResponseEntity.noContent().build();
    }

    /*
     * GET /api/tiendas/{id}/metricas
     * Devuelve el dashboard de metricas de una tienda
     * Solo el dueno puede verlo (validado en el servicio)
     *
     * Header: X-Usuario-Id: 5  ← id del usuario autenticado
     */
    @GetMapping("/{id}/metricas")
    @Operation(summary = "Métricas de tienda", description = "Dashboard de métricas de la tienda. Solo el dueño puede consultarlo.")
    public ResponseEntity<MetricaResponse> obtenerMetricas(
            @PathVariable Integer id,
            @RequestAttribute("X-Usuario-Id") Integer idUsuarioDueno) {

        MetricaResponse metricas = tiendaServicio.obtenerMetricas(id, idUsuarioDueno);
        return ResponseEntity.ok(metricas);
    }

}