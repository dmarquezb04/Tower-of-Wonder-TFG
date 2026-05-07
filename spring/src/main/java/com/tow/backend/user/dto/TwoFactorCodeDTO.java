package com.tow.backend.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para enviar el cÃ³digo TOTP de confirmaciÃ³n en operaciones de 2FA.
 * Recibido en {@code POST /user/2fa/enable} y {@code POST /user/2fa/disable}.
 *
 * @author DarÃ­o MÃ¡rquez Bautista
 */
@Data
@NoArgsConstructor
public class TwoFactorCodeDTO {

    /**
     * CÃ³digo TOTP de 6 dÃ­gitos generado por Google Authenticator.
     * VÃ¡lido solo durante Â±30 segundos desde su generaciÃ³n.
     */
    @NotBlank(message = "El cÃ³digo de verificaciÃ³n es obligatorio")
    @Pattern(regexp = "^[0-9]{6}$", message = "El cÃ³digo debe tener exactamente 6 dÃ­gitos numÃ©ricos")
    private String code;

    /**
     * Secret Base32 provisional, enviado por el cliente al activar el 2FA.
     * Solo es obligatorio en el endpoint {@code /2fa/enable}.
     */
    @NotBlank(message = "El secret de autenticaciÃ³n es obligatorio")
    private String secret;
}

