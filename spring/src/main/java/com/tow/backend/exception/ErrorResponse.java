package com.tow.backend.exception;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * DTO para enviar respuestas de error consistentes al frontend.
 *
 * <p>El campo {@code error} contiene el mensaje principal del error.
 * El campo {@code validationErrors} se rellena solo cuando hay errores de validación
 * de campos (@Valid), indicando qué campo específico falló y por qué.
 *
 * <p>Estructura JSON de ejemplo:
 * <pre>
 * {
 *   "error": "Errores de validación en la petición",
 *   "status": 400,
 *   "timestamp": "2026-05-07T16:00:00",
 *   "path": "/auth/register",
 *   "validationErrors": {
 *     "email": "El email no tiene un formato válido",
 *     "password": "La contraseña debe tener al menos 8 caracteres"
 *   }
 * }
 * </pre>
 *
 * @author Darío Márquez Bautista
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {

    /** Mensaje de error principal legible para el usuario. */
    private String error;

    /** Código de estado HTTP numérico (ej. 400, 401, 404). */
    private int status;

    /** Fecha y hora en que se produjo el error. */
    private LocalDateTime timestamp;

    /** Ruta de la petición que generó el error. */
    private String path;

    /**
     * Mapa de errores de validación por campo.
     * Solo presente en respuestas 400 de validación de formularios.
     * Clave: nombre del campo; Valor: mensaje de error.
     */
    private Map<String, String> validationErrors;
}

