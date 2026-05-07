package com.tow.backend.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO para la verificaciÃ³n del cÃ³digo TOTP de 2FA.
 * Recibido en {@code POST /api/auth/verify-2fa}.
 *
 * <p>El cliente debe incluir tambiÃ©n el token temporal (obtenido en el login)
 * en el header {@code Authorization: Bearer <token-temporal>}.
 */
@Getter
@Setter
public class TwoFactorRequest {

    /**
     * CÃ³digo TOTP de 6 dÃ­gitos generado por Google Authenticator.
     * VÃ¡lido solo durante Â±30 segundos.
     */
    @NotBlank(message = "El cÃ³digo de verificaciÃ³n es obligatorio")
    @Pattern(regexp = "^[0-9]{6}$", message = "El cÃ³digo debe tener exactamente 6 dÃ­gitos")
    private String code;
}

