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
import cl.duoc.ms_tiendas.service.TiendaServicio;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/tiendas")
@RequiredArgsConstructor
public class TiendaControlador {

    // El controlador solo conoce al servicio, nunca al repositorio directamente
    private final TiendaServicio tiendaServicio;

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
    public ResponseEntity<TiendaResponse> crearTienda(
            @RequestBody TiendaRequest solicitud,
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
    public ResponseEntity<TiendaResponse> actualizarTienda(
            @PathVariable Integer id,
            @RequestBody TiendaRequest solicitud,
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
    public ResponseEntity<TiendaResumenDTO> obtenerResumen(@PathVariable Integer id) {
        TiendaResumenDTO resumen = tiendaServicio.obtenerResumenParaOtrosMs(id);
        return ResponseEntity.ok(resumen);
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
    public ResponseEntity<MetricaResponse> obtenerMetricas(
            @PathVariable Integer id,
            @RequestAttribute("X-Usuario-Id") Integer idUsuarioDueno) {

        MetricaResponse metricas = tiendaServicio.obtenerMetricas(id, idUsuarioDueno);
        return ResponseEntity.ok(metricas);
    }

}