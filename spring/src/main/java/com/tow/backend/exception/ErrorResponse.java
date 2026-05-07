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
 * El campo {@code validationErrors} se rellena solo cuando hay errores de validaciÃ³n
 * de campos (@Valid), indicando quÃ© campo especÃ­fico fallÃ³ y por quÃ©.
 *
 * <p>Estructura JSON de ejemplo:
 * <pre>
 * {
 *   "error": "Errores de validaciÃ³n en la peticiÃ³n",
 *   "status": 400,
 *   "timestamp": "2026-05-07T16:00:00",
 *   "path": "/auth/register",
 *   "validationErrors": {
 *     "email": "El email no tiene un formato vÃ¡lido",
 *     "password": "La contraseÃ±a debe tener al menos 8 caracteres"
 *   }
 * }
 * </pre>
 *
 * @author DarÃ­o MÃ¡rquez Bautista
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {

    /** Mensaje de error principal legible para el usuario. */
    private String error;

    /** CÃ³digo de estado HTTP numÃ©rico (ej. 400, 401, 404). */
    private int status;

    /** Fecha y hora en que se produjo el error. */
    private LocalDateTime timestamp;

    /** Ruta de la peticiÃ³n que generÃ³ el error. */
    private String path;

    /**
     * Mapa de errores de validaciÃ³n por campo.
     * Solo presente en respuestas 400 de validaciÃ³n de formularios.
     * Clave: nombre del campo; Valor: mensaje de error.
     */
    private Map<String, String> validationErrors;
}

