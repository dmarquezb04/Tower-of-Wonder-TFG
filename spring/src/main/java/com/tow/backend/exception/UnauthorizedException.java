package com.tow.backend.exception;

import org.springframework.http.HttpStatus;

/**
 * Excepción lanzada cuando el usuario no está autenticado o sus credenciales son incorrectas.
 *
 * <p>Casos típicos: contraseña incorrecta, token 2FA inválido, cuenta desactivada.
 * Produce una respuesta HTTP {@code 401 Unauthorized}.
 *
 * <p>Nota: no confundir con {@code 403 Forbidden}, que es para accesos denegados por falta
 * de permisos (eso lo gestiona Spring Security con {@code AccessDeniedException}).
 *
 * @author Darío Márquez Bautista
 */
public class UnauthorizedException extends CustomException {

    /**
     * @param message mensaje de error (ej. "Credenciales incorrectas")
     */
    public UnauthorizedException(String message) {
        super(message, HttpStatus.UNAUTHORIZED);
    }
}

