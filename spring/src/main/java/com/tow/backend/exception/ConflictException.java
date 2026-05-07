package com.tow.backend.exception;

import org.springframework.http.HttpStatus;

/**
 * Excepción lanzada cuando existe un conflicto con el estado actual del recurso.
 *
 * <p>Casos típicos: email o nombre de usuario ya registrado, suscripción ya confirmada.
 * Produce una respuesta HTTP {@code 409 Conflict}.
 *
 * @author Darío Márquez Bautista
 */
public class ConflictException extends CustomException {

    /**
     * @param message descripción del conflicto (ej. "El email ya está registrado")
     */
    public ConflictException(String message) {
        super(message, HttpStatus.CONFLICT);
    }
}

