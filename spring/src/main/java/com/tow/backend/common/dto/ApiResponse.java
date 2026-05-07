package com.tow.backend.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO estándar para respuestas de operaciones exitosas que solo devuelven un mensaje.
 *
 * <p>Se usa cuando la operación se completa correctamente pero no hay un objeto de datos
 * que retornar (ej. logout, eliminación, activación de 2FA).
 *
 * <p>Ejemplo de respuesta JSON:
 * <pre>
 * { "message": "Sesión cerrada correctamente" }
 * </pre>
 *
 * @author Darío Márquez Bautista
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse {

    /** Mensaje descriptivo del resultado de la operación. */
    private String message;
}

