package com.tow.backend.exception;

import org.springframework.http.HttpStatus;

/**
 * Excepción lanzada cuando la petición del cliente es semánticamente incorrecta.
 *
 * <p>Casos típicos: carrito vacío en checkout, token de recuperación caducado,
 * intento de activar 2FA cuando ya está activo.
 * Produce una respuesta HTTP {@code 400 Bad Request}.
 *
 * @author Darío Márquez Bautista
 */
public class BadRequestException extends CustomException {

    /**
     * @param message descripción del error (ej. "El carrito está vacío")
     */
    public BadRequestException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}

