package com.tow.backend.newsletter.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * DTO para la petición de suscripción a la newsletter.
 * Recibido en {@code POST /newsletter/subscribe}.
 *
 * @author Darío Márquez Bautista
 */
@Data
public class SubscribeRequest {

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El email no tiene un formato válido")
    private String email;
}

