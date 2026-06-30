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
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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

    @GetMapping
    @Operation(summary = "Listar tiendas activas", description = "Devuelve todas las tiendas con estado ACTIVA.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de tiendas activas"),
            @ApiResponse(responseCode = "401", description = "Token ausente, inválido o expirado")
    })
    public ResponseEntity<List<TiendaResponse>> listarTiendas(
            @Parameter(description = "Token JWT con formato 'Bearer {token}'", required = true)
            @RequestHeader("Authorization") String tokenJwt) {

        List<TiendaResponse> tiendas = tiendaServicio.listarTiendasActivas(tokenJwt);
        return ResponseEntity.ok(tiendas);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Ver tienda por ID", description = "Obtiene el perfil completo de una tienda y suma una visita a sus métricas.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tienda encontrada"),
            @ApiResponse(responseCode = "401", description = "Token ausente, inválido o expirado"),
            @ApiResponse(responseCode = "404", description = "La tienda no existe")
    })
    public ResponseEntity<TiendaResponse> obtenerTienda(
            @Parameter(description = "ID de la tienda", required = true, example = "1")
            @PathVariable Integer id,
            @Parameter(description = "Token JWT con formato 'Bearer {token}'", required = true)
            @RequestHeader("Authorization") String tokenJwt) {

        TiendaResponse tienda = tiendaServicio.obtenerPorId(id, tokenJwt);
        return ResponseEntity.ok(tienda);
    }

    @PostMapping
    @Operation(summary = "Crear tienda", description = "Crea una nueva tienda para el usuario autenticado (rol TIENDA requerido).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Tienda creada", content = @Content(
                    examples = @ExampleObject(name = "TiendaCreada", value = """
                            {
                              "id": 1,
                              "nombre": "Carta Magica TCG",
                              "descripcion": "Especialistas en Magic the Gathering y Pokemon",
                              "telefono": "+56912345678",
                              "emailContacto": "contacto@cartamagica.cl",
                              "horarioAtencion": "Lun-Sab 11:00-20:00",
                              "estado": "ACTIVA"
                            }
                            """))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "401", description = "Token ausente, inválido, o rol distinto de TIENDA")
    })
    public ResponseEntity<TiendaResponse> crearTienda(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Datos de la tienda a crear", required = true,
                    content = @Content(examples = @ExampleObject(value = """
                            {
                              "nombre": "Carta Magica TCG",
                              "descripcion": "Especialistas en Magic the Gathering y Pokemon",
                              "telefono": "+56912345678",
                              "emailContacto": "contacto@cartamagica.cl",
                              "horarioAtencion": "Lun-Sab 11:00-20:00"
                            }
                            """)))
            @Valid @RequestBody TiendaRequest solicitud,
            @Parameter(description = "Token JWT con formato 'Bearer {token}'", required = true)
            @RequestHeader("Authorization") String tokenJwt,
            @Parameter(hidden = true)
            @RequestAttribute("X-Usuario-Id") Integer idUsuarioDueno) {

        TiendaResponse tiendaCreada = tiendaServicio.crearTienda(solicitud, idUsuarioDueno, tokenJwt);
        // 201 Created en lugar de 200 OK porque estamos creando un recurso nuevo
        return ResponseEntity.status(HttpStatus.CREATED).body(tiendaCreada);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar tienda", description = "Actualiza los datos de la tienda. Solo el dueño puede hacerlo.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tienda actualizada correctamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "401", description = "Token ausente, inválido, o el usuario no es el dueño de la tienda")
    })
    public ResponseEntity<TiendaResponse> actualizarTienda(
            @Parameter(description = "ID de la tienda a actualizar", required = true, example = "1")
            @PathVariable Integer id,
            @Valid @RequestBody TiendaRequest solicitud,
            @Parameter(description = "Token JWT con formato 'Bearer {token}'", required = true)
            @RequestHeader("Authorization") String tokenJwt,
            @Parameter(hidden = true)
            @RequestAttribute("X-Usuario-Id") Integer idUsuarioDueno) {

        TiendaResponse tiendaActualizada = tiendaServicio.actualizarTienda(id, solicitud, idUsuarioDueno, tokenJwt);
        return ResponseEntity.ok(tiendaActualizada);
    }

    // ================================================================
    // ENDPOINTS INTERNOS (solo para otros microservicios de CardLink)
    // Header requerido: Authorization: Bearer <token>
    // ================================================================

    @GetMapping("/{id}/resumen")
    @Operation(summary = "Resumen de tienda (interno)", description = "Devuelve datos básicos de la tienda. Usado por ms-inventario, ms-localizacion y ms-eventos via Feign.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Resumen de la tienda"),
            @ApiResponse(responseCode = "404", description = "La tienda no existe")
    })
    public ResponseEntity<TiendaResumenDTO> obtenerResumen(
            @Parameter(description = "ID de la tienda", required = true, example = "1")
            @PathVariable Integer id) {
        TiendaResumenDTO resumen = tiendaServicio.obtenerResumenParaOtrosMs(id);
        return ResponseEntity.ok(resumen);
    }

    @GetMapping("/dueno/{usuarioId}")
    @Operation(summary = "Tiendas por dueño (interno)", description = "Devuelve las tiendas que pertenecen a un usuario. Usado por ms-eventos via Feign.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de tiendas del usuario (puede ser vacía)")
    })
    public ResponseEntity<List<TiendaResumenDTO>> obtenerTiendasPorDueno(
            @Parameter(description = "ID del usuario dueño (id de ms-login)", required = true, example = "5")
            @PathVariable Integer usuarioId) {
        List<TiendaResumenDTO> tiendas = tiendaServicio.obtenerResumenesPorDueno(usuarioId);
        return ResponseEntity.ok(tiendas);
    }

    @PutMapping("/{id}/sumar-evento")
    @Operation(summary = "Sumar evento a tienda (interno)", description = "Incrementa el contador de eventos de la tienda. Llamado por ms-eventos via Feign.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Contador incrementado correctamente, sin contenido de respuesta"),
            @ApiResponse(responseCode = "404", description = "La tienda no existe")
    })
    public ResponseEntity<Void> sumarEvento(
            @Parameter(description = "ID de la tienda", required = true, example = "1")
            @PathVariable Integer id) {
        tiendaServicio.incrementarTotalEventos(id);
        // 204 No Content: operacion exitosa pero sin cuerpo de respuesta
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/metricas")
    @Operation(summary = "Métricas de tienda", description = "Dashboard de métricas de la tienda. Solo el dueño puede consultarlo.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Métricas de la tienda"),
            @ApiResponse(responseCode = "401", description = "El usuario no es el dueño de la tienda")
    })
    public ResponseEntity<MetricaResponse> obtenerMetricas(
            @Parameter(description = "ID de la tienda", required = true, example = "1")
            @PathVariable Integer id,
            @Parameter(hidden = true)
            @RequestAttribute("X-Usuario-Id") Integer idUsuarioDueno) {

        MetricaResponse metricas = tiendaServicio.obtenerMetricas(id, idUsuarioDueno);
        return ResponseEntity.ok(metricas);
    }

}
