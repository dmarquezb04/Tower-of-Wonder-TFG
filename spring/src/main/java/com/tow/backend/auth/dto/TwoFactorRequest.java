package com.tow.backend.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO para la verificación del código TOTP de 2FA.
 * Recibido en {@code POST /api/auth/verify-2fa}.
 *
 * <p>El cliente debe incluir también el token temporal (obtenido en el login)
 * en el header {@code Authorization: Bearer <token-temporal>}.
 */
@Getter
@Setter
public class TwoFactorRequest {

    /**
     * Código TOTP de 6 dígitos generado por Google Authenticator.
     * Válido solo durante ±30 segundos.
     */
    @NotBlank(message = "El código de verificación es obligatorio")
    @Pattern(regexp = "^[0-9]{6}$", message = "El código debe tener exactamente 6 dígitos")
    private String code;
}

