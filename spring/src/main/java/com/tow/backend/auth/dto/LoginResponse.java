package com.tow.backend.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * DTO de respuesta al login.
 * Devuelto por {@code POST /api/auth/login}.
 *
 * <p>Dos escenarios posibles:
 * <ol>
 *   <li>Login completo (sin 2FA o 2FA desactivado):
 *       {@code requiresTwoFactor=false}, {@code token} = JWT completo (24h)</li>
 *   <li>2FA requerido:
 *       {@code requiresTwoFactor=true}, {@code token} = JWT temporal (5min),
 *       el cliente debe hacer {@code POST /api/auth/verify-2fa}</li>
 * </ol>
 */
@Getter
@Builder
@AllArgsConstructor
public class LoginResponse {

    /** Si se requiere verificaciÃ³n de 2FA para completar el login. */
    private boolean requiresTwoFactor;

    /**
     * JWT resultante.
     * <ul>
     *   <li>Si {@code requiresTwoFactor=false}: token completo de 24h</li>
     *   <li>Si {@code requiresTwoFactor=true}: token temporal de 5min</li>
     * </ul>
     */
    private String token;

    /** Email del usuario autenticado. */
    private String email;

    /** Nombre de usuario. */
    private String username;

    /** Mensaje informativo opcional. */
    private String message;
}

