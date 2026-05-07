package com.tow.backend.exception;

import org.springframework.http.HttpStatus;

/**
 * ExcepciÃ³n lanzada cuando existe un conflicto con el estado actual del recurso.
 *
 * <p>Casos tÃ­picos: email o nombre de usuario ya registrado, suscripciÃ³n ya confirmada.
 * Produce una respuesta HTTP {@code 409 Conflict}.
 *
 * @author DarÃ­o MÃ¡rquez Bautista
 */
public class ConflictException extends CustomException {

    /**
     * @param message descripciÃ³n del conflicto (ej. "El email ya estÃ¡ registrado")
     */
    public ConflictException(String message) {
        super(message, HttpStatus.CONFLICT);
    }
}

