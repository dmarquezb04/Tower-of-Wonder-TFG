package com.tow.backend.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para enviar el código TOTP de confirmación en operaciones de 2FA.
 * Recibido en {@code POST /user/2fa/enable} y {@code POST /user/2fa/disable}.
 *
 * @author Darío Márquez Bautista
 */
@Data
@NoArgsConstructor
public class TwoFactorCodeDTO {

    /**
     * Código TOTP de 6 dígitos generado por Google Authenticator.
     * Válido solo durante ±30 segundos desde su generación.
     */
    @NotBlank(message = "El código de verificación es obligatorio")
    @Pattern(regexp = "^[0-9]{6}$", message = "El código debe tener exactamente 6 dígitos numéricos")
    private String code;

    /**
     * Secret Base32 provisional, enviado por el cliente al activar el 2FA.
     * Solo es obligatorio en el endpoint {@code /2fa/enable}.
     */
    @NotBlank(message = "El secret de autenticación es obligatorio")
    private String secret;
}

