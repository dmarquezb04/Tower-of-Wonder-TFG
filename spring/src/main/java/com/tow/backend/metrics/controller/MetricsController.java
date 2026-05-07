package com.tow.backend.metrics.controller;

import com.tow.backend.metrics.dto.TrackVisitRequest;
import com.tow.backend.metrics.service.MetricsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controlador REST para el registro y consulta de mÃ©tricas de uso.
 *
 * <p>El registro de visitas es pÃºblico (para el frontend).
 * La consulta de estadÃ­sticas requiere el rol ADMIN.
 *
 * @author DarÃ­o MÃ¡rquez Bautista
 */
@RestController
@RequestMapping("/metrics")
@RequiredArgsConstructor
@Tag(name = "MÃ©tricas", description = "Seguimiento de visitas y estadÃ­sticas del sistema")
public class MetricsController {

    private final MetricsService metricsService;

    /**
     * Registra una visita a una URL del sitio web.
     *
     * @param request body con la URL visitada
     * @param httpRequest objeto de la peticiÃ³n para extraer la IP
     * @return 200 OK vacÃ­o
     */
    @PostMapping("/track")
    @Operation(summary = "Registrar visita a una URL (PÃºblico)")
    public ResponseEntity<Void> track(@RequestBody TrackVisitRequest request, HttpServletRequest httpRequest) {
        if (request.getUrl() != null && !request.getUrl().startsWith("/admin")) {
            metricsService.trackVisit(request.getUrl(), httpRequest);
        }
        return ResponseEntity.ok().build();
    }

    /**
     * Devuelve las estadÃ­sticas agregadas de visitas.
     *
     * @return 200 OK con mapa de estadÃ­sticas
     */
    @GetMapping("/admin/stats")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Obtener estadÃ­sticas de visitas (Admin)")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "EstadÃ­sticas devueltas correctamente"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Acceso denegado â€” se requiere rol ADMIN")
    })
    public ResponseEntity<Map<String, Object>> getAdminStats() {
        return ResponseEntity.ok(metricsService.getStats());
    }
}


