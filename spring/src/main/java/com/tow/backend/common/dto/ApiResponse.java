package com.tow.backend.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO estÃ¡ndar para respuestas de operaciones exitosas que solo devuelven un mensaje.
 *
 * <p>Se usa cuando la operaciÃ³n se completa correctamente pero no hay un objeto de datos
 * que retornar (ej. logout, eliminaciÃ³n, activaciÃ³n de 2FA).
 *
 * <p>Ejemplo de respuesta JSON:
 * <pre>
 * { "message": "SesiÃ³n cerrada correctamente" }
 * </pre>
 *
 * @author DarÃ­o MÃ¡rquez Bautista
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse {

    /** Mensaje descriptivo del resultado de la operaciÃ³n. */
    private String message;
}

