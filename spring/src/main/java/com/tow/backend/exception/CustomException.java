package com.tow.backend.exception;

import org.springframework.http.HttpStatus;

/**
 * Excepción base para todas las excepciones de negocio personalizadas de la aplicación.
 *
 * <p>Todas las excepciones custom deben extender esta clase. El {@link GlobalExceptionHandler}
 * captura esta jerarquía y convierte los errores en respuestas HTTP estructuradas,
 * usando el {@link HttpStatus} que cada subclase defina.
 *
 * @author Darío Márquez Bautista
 */
public class CustomException extends RuntimeException {

    private final HttpStatus status;

    /**
     * Construye una excepción personalizada con mensaje y código de estado HTTP.
     *
     * @param message mensaje de error legible para el usuario
     * @param status  código de estado HTTP que debe enviarse al cliente
     */
    public CustomException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    /**
     * Devuelve el código de estado HTTP asociado a esta excepción.
     *
     * @return código de estado HTTP
     */
    public HttpStatus getStatus() {
        return status;
    }
}

