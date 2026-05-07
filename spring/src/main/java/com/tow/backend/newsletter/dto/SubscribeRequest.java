package com.tow.backend.newsletter.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * DTO para la peticiÃ³n de suscripciÃ³n a la newsletter.
 * Recibido en {@code POST /newsletter/subscribe}.
 *
 * @author DarÃ­o MÃ¡rquez Bautista
 */
@Data
public class SubscribeRequest {

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El email no tiene un formato vÃ¡lido")
    private String email;
}

