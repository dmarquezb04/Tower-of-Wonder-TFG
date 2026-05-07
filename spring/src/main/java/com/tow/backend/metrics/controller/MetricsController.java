package com.tow.backend.metrics.controller;

import com.tow.backend.metrics.dto.TrackVisitRequest;
import com.tow.backend.metrics.service.MetricsService;
import io.swagger.v3.oas.annotations.Operation;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controlador REST para el registro y consulta de métricas de uso.
 *
 * <p>El registro de visitas es público (para el frontend).
 * La consulta de estadísticas requiere el rol ADMIN.
 *
 * @author Darío Márquez Bautista
 */
@RestController
@RequestMapping("/metrics")
@RequiredArgsConstructor
@Tag(name = "Métricas", description = "Seguimiento de visitas y estadísticas del sistema")
public class MetricsController {

    private final MetricsService metricsService;

    /**
     * Registra una visita a una URL del sitio web.
     *
     * @param request body con la URL visitada
     * @param httpRequest objeto de la petición para extraer la IP
     * @return 200 OK vacío
     */
    @PostMapping("/track")
    @Operation(summary = "Registrar visita a una URL (Público)")
    public ResponseEntity<Void> track(@RequestBody TrackVisitRequest request, HttpServletRequest httpRequest) {
        if (request.getUrl() != null && !request.getUrl().startsWith("/admin")) {
            metricsService.trackVisit(request.getUrl(), httpRequest);
        }
        return ResponseEntity.ok().build();
    }

    /**
     * Devuelve las estadísticas agregadas de visitas.
     *
     * @return 200 OK con mapa de estadísticas
     */
    @GetMapping("/admin/stats")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Obtener estadísticas de visitas (Admin)")
    public ResponseEntity<Map<String, Object>> getAdminStats() {
        return ResponseEntity.ok(metricsService.getStats());
    }

    /**
     * Exporta todas las métricas de visitas a un archivo Excel.
     *
     * @return 200 OK con el archivo Excel para descarga
     */
    @GetMapping("/admin/export")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Exportar métricas a Excel (Admin)")
    public ResponseEntity<Resource> exportMetrics() {
        byte[] data = metricsService.exportToExcel();
        ByteArrayResource resource = new ByteArrayResource(data);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=metricas_visitas.xlsx")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .contentLength(data.length)
                .body(resource);
    }
}
