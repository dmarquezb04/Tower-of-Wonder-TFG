package com.tow.backend.common.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Controlador de verificación de estado del sistema (health check).
 *
 * <p>Expone un endpoint público {@code GET /api/health} que permite verificar
 * que el servidor Spring Boot está activo y puede responder peticiones HTTP.
 *
 * <p>No requiere autenticación. Útil para:
 * <ul>
 *   <li>Verificar el despliegue en Docker</li>
 *   <li>Comprobaciones de monitorización</li>
 *   <li>Validar la configuración de nginx</li>
 * </ul>
 *
 * @author Darío Márquez Bautista
 */
@RestController
@RequestMapping("/health")
public class HealthController {

    /**
     * Devuelve el estado del servidor y la hora actual del sistema.
     *
     * @return 200 OK con JSON {@code {"status": "UP", "timestamp": "..."}}
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "Tower of Wonder API",
                "timestamp", LocalDateTime.now().toString()
        ));
    }
}
