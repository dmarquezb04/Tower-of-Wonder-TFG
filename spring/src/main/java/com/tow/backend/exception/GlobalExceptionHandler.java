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
 *
 * <p>Captura todas las excepciones lanzadas en la capa de controladores y servicios,
 * y las transforma en una respuesta JSON estandarizada ({@link ErrorResponse}).
 * Esto evita que el frontend reciba errores 500 genÃ©ricos o trazas de pila internas.
 *
 * <p>Los handlers estÃ¡n organizados en tres grupos:
 * <ol>
 *   <li><strong>Custom</strong>: excepciones de negocio que heredan de {@link CustomException}.</li>
 *   <li><strong>Spring/Jakarta</strong>: errores de validaciÃ³n, seguridad y protocolo HTTP.</li>
 *   <li><strong>GenÃ©rico</strong>: Ãºltimo recurso para cualquier error no previsto.</li>
 * </ol>
 *
 * <p>Todos los errores internos (5xx) se registran en los logs mediante SLF4J.
 *
 * @author DarÃ­o MÃ¡rquez Bautista
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // =========================================================================
    // GRUPO 1: Excepciones de negocio custom (heredan de CustomException)
    // =========================================================================

    /**
     * Captura cualquier excepciÃ³n que extienda {@link CustomException}.
     *
     * <p>Incluye: {@link NotFoundException} (404), {@link ConflictException} (409),
     * {@link BadRequestException} (400), {@link UnauthorizedException} (401).
     *
     * @param ex      excepciÃ³n de negocio
     * @param request peticiÃ³n HTTP actual
     * @return respuesta de error con el status definido en la excepciÃ³n
     */
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ErrorResponse> handleCustomException(CustomException ex, HttpServletRequest request) {
        log.warn("Error de negocio en {}: [{}] {}", request.getRequestURI(), ex.getStatus(), ex.getMessage());

        ErrorResponse error = buildError(ex.getMessage(), ex.getStatus().value(), request.getRequestURI());
        return new ResponseEntity<>(error, ex.getStatus());
    }

    // =========================================================================
    // GRUPO 2: Excepciones de Spring Security / ValidaciÃ³n / HTTP
    // =========================================================================

    /**
     * Captura errores de autenticaciÃ³n lanzados por Spring Security.
     *
     * <p>Devuelve un mensaje genÃ©rico "Credenciales incorrectas" para no revelar
     * si el email existe en el sistema o no.
     *
     * @param ex      excepciÃ³n de credenciales incorrectas
     * @param request peticiÃ³n HTTP actual
     * @return 401 Unauthorized con mensaje seguro
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(BadCredentialsException ex, HttpServletRequest request) {
        log.warn("Intento de autenticaciÃ³n fallido en {}: {}", request.getRequestURI(), ex.getMessage());

        ErrorResponse error = buildError("Credenciales incorrectas", HttpStatus.UNAUTHORIZED.value(), request.getRequestURI());
        return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
    }

    /**
     * Captura intentos de acceso a recursos sin los permisos necesarios.
     *
     * <p>Se activa cuando un usuario autenticado (con token vÃ¡lido) intenta acceder
     * a un endpoint que requiere un rol superior (ej. un usuario normal accede a /admin/).
     *
     * @param ex      excepciÃ³n de acceso denegado
     * @param request peticiÃ³n HTTP actual
     * @return 403 Forbidden
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
        log.warn("Acceso denegado en {}: {}", request.getRequestURI(), ex.getMessage());

        ErrorResponse error = buildError("No tienes permisos para realizar esta acciÃ³n", HttpStatus.FORBIDDEN.value(), request.getRequestURI());
        return new ResponseEntity<>(error, HttpStatus.FORBIDDEN);
    }

    /**
     * Captura errores de validaciÃ³n de campos cuando se usa {@code @Valid} en el controlador.
     *
     * <p>Recoge todos los errores de todos los campos y los devuelve en el mapa
     * {@code validationErrors} del {@link ErrorResponse}.
     *
     * @param ex      excepciÃ³n con la lista de errores de campo
     * @param request peticiÃ³n HTTP actual
     * @return 400 Bad Request con mapa de errores por campo
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(MethodArgumentNotValidException ex, HttpServletRequest request) {
        Map<String, String> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        fieldError -> fieldError.getField(),
                        fieldError -> fieldError.getDefaultMessage() != null ? fieldError.getDefaultMessage() : "Valor no vÃ¡lido",
                        (existing, replacement) -> existing
                ));

        log.debug("Error de validaciÃ³n en {}: {}", request.getRequestURI(), fieldErrors);

        ErrorResponse error = ErrorResponse.builder()
                .error("Errores de validaciÃ³n en la peticiÃ³n")
                .status(HttpStatus.BAD_REQUEST.value())
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .validationErrors(fieldErrors)
                .build();

        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    /**
     * Captura el uso de un mÃ©todo HTTP incorrecto en un endpoint (ej. GET en vez de POST).
     *
     * @param ex      excepciÃ³n con el mÃ©todo no soportado
     * @param request peticiÃ³n HTTP actual
     * @return 405 Method Not Allowed
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {
        log.warn("MÃ©todo HTTP no soportado en {}: {}", request.getRequestURI(), ex.getMessage());

        ErrorResponse error = buildError(
                "MÃ©todo HTTP no soportado: " + ex.getMethod(),
                HttpStatus.METHOD_NOT_ALLOWED.value(),
                request.getRequestURI()
        );
        return new ResponseEntity<>(error, HttpStatus.METHOD_NOT_ALLOWED);
    }

    /**
     * Captura errores al deserializar el cuerpo JSON de la peticiÃ³n.
     *
     * <p>Sucede cuando el JSON estÃ¡ mal formado o los tipos de datos no coinciden
     * con lo esperado por el DTO.
     *
     * @param ex      excepciÃ³n de lectura del mensaje
     * @param request peticiÃ³n HTTP actual
     * @return 400 Bad Request
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleMessageNotReadable(HttpMessageNotReadableException ex, HttpServletRequest request) {
        log.warn("Error al leer el cuerpo de la peticiÃ³n en {}: {}", request.getRequestURI(), ex.getMessage());

        ErrorResponse error = buildError("El cuerpo de la peticiÃ³n es invÃ¡lido o estÃ¡ mal formado", HttpStatus.BAD_REQUEST.value(), request.getRequestURI());
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    /**
     * Captura errores de tipo en parÃ¡metros de ruta o query (ej. se espera Long y llega "abc").
     *
     * @param ex      excepciÃ³n de tipo incorrecto
     * @param request peticiÃ³n HTTP actual
     * @return 400 Bad Request
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        log.warn("Tipo de parÃ¡metro incorrecto en {}: parÃ¡metro '{}', valor '{}'", request.getRequestURI(), ex.getName(), ex.getValue());

        ErrorResponse error = buildError(
                "ParÃ¡metro '" + ex.getName() + "' tiene un valor no vÃ¡lido",
                HttpStatus.BAD_REQUEST.value(),
                request.getRequestURI()
        );
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    /**
     * Captura la ausencia de un header requerido en la peticiÃ³n.
     *
     * <p>Caso tÃ­pico: falta el header {@code Authorization} en un endpoint protegido.
     *
     * @param ex      excepciÃ³n de header ausente
     * @param request peticiÃ³n HTTP actual
     * @return 400 Bad Request
     */
    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<ErrorResponse> handleMissingHeader(MissingRequestHeaderException ex, HttpServletRequest request) {
        log.warn("Header requerido ausente en {}: {}", request.getRequestURI(), ex.getHeaderName());

        ErrorResponse error = buildError(
                "Falta el header requerido: " + ex.getHeaderName(),
                HttpStatus.BAD_REQUEST.value(),
                request.getRequestURI()
        );
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    // =========================================================================
    // GRUPO 3: Ãšltimo recurso â€” errores no previstos
    // =========================================================================

    /**
     * Captura cualquier excepciÃ³n no controlada por los handlers anteriores.
     *
     * <p>El error completo se registra en los logs pero el mensaje devuelto al cliente
     * es genÃ©rico para no exponer informaciÃ³n interna del servidor.
     *
     * @param ex      excepciÃ³n no prevista
     * @param request peticiÃ³n HTTP actual
     * @return 500 Internal Server Error con mensaje genÃ©rico
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobalException(Exception ex, HttpServletRequest request) {
        log.error("ExcepciÃ³n no controlada en {}: ", request.getRequestURI(), ex);

        ErrorResponse error = buildError("Ha ocurrido un error inesperado. IntÃ©ntalo de nuevo mÃ¡s tarde.", HttpStatus.INTERNAL_SERVER_ERROR.value(), request.getRequestURI());
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // =========================================================================
    // MÃ©todo auxiliar privado
    // =========================================================================

    /**
     * Construye un {@link ErrorResponse} estÃ¡ndar sin errores de validaciÃ³n.
     *
     * @param message mensaje de error
     * @param status  cÃ³digo de estado HTTP numÃ©rico
     * @param path    ruta de la peticiÃ³n
     * @return objeto {@link ErrorResponse} listo para serializar
     */
    private ErrorResponse buildError(String message, int status, String path) {
        return ErrorResponse.builder()
                .error(message)
                .status(status)
                .timestamp(LocalDateTime.now())
                .path(path)
                .build();
    }
}

