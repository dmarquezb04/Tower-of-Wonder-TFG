package com.tow.backend.metrics.controller;

import com.tow.backend.metrics.dto.TrackVisitRequest;
import com.tow.backend.metrics.service.MetricsService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/metrics")
@RequiredArgsConstructor
public class MetricsController {

    private final MetricsService metricsService;

    // Endpoint PÚBLICO para registrar visitas desde el frontend
    @PostMapping("/track")
    public ResponseEntity<Void> track(@RequestBody TrackVisitRequest request, HttpServletRequest httpRequest) {
        // No rastreamos si la URL es de admin (doble comprobación)
        if (!request.getUrl().startsWith("/admin")) {
            metricsService.trackVisit(request.getUrl(), httpRequest);
        }
        return ResponseEntity.ok().build();
    }

    // Endpoint PRIVADO para el dashboard de admin
    @GetMapping("/admin/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getAdminStats() {
        return ResponseEntity.ok(metricsService.getStats());
    }
}
