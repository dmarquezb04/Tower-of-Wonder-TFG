package com.tow.backend.exception;

import org.springframework.http.HttpStatus;

/**
 * ExcepciÃ³n base para todas las excepciones de negocio personalizadas de la aplicaciÃ³n.
 *
 * <p>Todas las excepciones custom deben extender esta clase. El {@link GlobalExceptionHandler}
 * captura esta jerarquÃ­a y convierte los errores en respuestas HTTP estructuradas,
 * usando el {@link HttpStatus} que cada subclase defina.
 *
 * @author DarÃ­o MÃ¡rquez Bautista
 */
public class CustomException extends RuntimeException {

    private final HttpStatus status;

    /**
     * Construye una excepciÃ³n personalizada con mensaje y cÃ³digo de estado HTTP.
     *
     * @param message mensaje de error legible para el usuario
     * @param status  cÃ³digo de estado HTTP que debe enviarse al cliente
     */
    public CustomException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    /**
     * Devuelve el cÃ³digo de estado HTTP asociado a esta excepciÃ³n.
     *
     * @return cÃ³digo de estado HTTP
     */
    public HttpStatus getStatus() {
        return status;
    }
}

