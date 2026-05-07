package com.tow.backend.exception;

import org.springframework.http.HttpStatus;

/**
 * ExcepciÃ³n lanzada cuando la peticiÃ³n del cliente es semÃ¡nticamente incorrecta.
 *
 * <p>Casos tÃ­picos: carrito vacÃ­o en checkout, token de recuperaciÃ³n caducado,
 * intento de activar 2FA cuando ya estÃ¡ activo.
 * Produce una respuesta HTTP {@code 400 Bad Request}.
 *
 * @author DarÃ­o MÃ¡rquez Bautista
 */
public class BadRequestException extends CustomException {

    /**
     * @param message descripciÃ³n del error (ej. "El carrito estÃ¡ vacÃ­o")
     */
    public BadRequestException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}

