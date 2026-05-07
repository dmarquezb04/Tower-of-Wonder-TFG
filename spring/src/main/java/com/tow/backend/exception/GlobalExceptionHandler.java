package com.tow.backend.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Gestor global de excepciones.
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // 1. Excepciones de negocio (400, 401, 404, 409)
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ErrorResponse> handleCustom(CustomException ex, HttpServletRequest req) {
        return buildResponse(ex.getMessage(), ex.getStatus(), req);
    }

    // 2. Errores de validación de campos (@Valid)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest req) {
        Map<String, String> errors = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(f -> f.getField(),
                        f -> f.getDefaultMessage() != null ? f.getDefaultMessage() : "Error", (a, b) -> a));

        return new ResponseEntity<>(ErrorResponse.builder()
                .error("Error de validación")
                .status(HttpStatus.BAD_REQUEST.value())
                .timestamp(LocalDateTime.now())
                .path(req.getRequestURI())
                .validationErrors(errors).build(), HttpStatus.BAD_REQUEST);
    }

    // 3. Errores de seguridad (401, 403)
    @ExceptionHandler({ BadCredentialsException.class, AccessDeniedException.class })
    public ResponseEntity<ErrorResponse> handleSecurity(Exception ex, HttpServletRequest req) {
        HttpStatus status = (ex instanceof BadCredentialsException) ? HttpStatus.UNAUTHORIZED : HttpStatus.FORBIDDEN;
        String msg = (ex instanceof BadCredentialsException) ? "Credenciales incorrectas" : "No tienes permisos";
        return buildResponse(msg, status, req);
    }

    // 4. Errores de cliente (400, 405) agrupados
    @ExceptionHandler({
            HttpMessageNotReadableException.class,
            MethodArgumentTypeMismatchException.class,
            MissingRequestHeaderException.class,
            HttpRequestMethodNotSupportedException.class,
            NumberFormatException.class
    })
    public ResponseEntity<ErrorResponse> handleClientErrors(Exception ex, HttpServletRequest req) {
        HttpStatus status = (ex instanceof HttpRequestMethodNotSupportedException) ? HttpStatus.METHOD_NOT_ALLOWED
                : HttpStatus.BAD_REQUEST;
        return buildResponse("Petición incorrecta o mal formada", status, req);
    }

    // 5. Error genérico (500)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobal(Exception ex, HttpServletRequest req) {
        log.error("Error no controlado en {}: ", req.getRequestURI(), ex);
        return buildResponse("Error interno en el servidor", HttpStatus.INTERNAL_SERVER_ERROR, req);
    }

    private ResponseEntity<ErrorResponse> buildResponse(String msg, HttpStatus status, HttpServletRequest req) {
        return new ResponseEntity<>(ErrorResponse.builder()
                .error(msg)
                .status(status.value())
                .timestamp(LocalDateTime.now())
                .path(req.getRequestURI()).build(), status);
    }
}
