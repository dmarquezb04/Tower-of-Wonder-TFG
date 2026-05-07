package com.tow.backend.exception;

import org.springframework.http.HttpStatus;

/**
 * ExcepciÃ³n lanzada cuando un recurso solicitado no se encuentra en la base de datos.
 *
 * <p>Produce una respuesta HTTP {@code 404 Not Found}.
 *
 * @author DarÃ­o MÃ¡rquez Bautista
 */
public class NotFoundException extends CustomException {

    /**
     * @param message descripciÃ³n del recurso no encontrado (ej. "Usuario no encontrado")
     */
    public NotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND);
    }
}

