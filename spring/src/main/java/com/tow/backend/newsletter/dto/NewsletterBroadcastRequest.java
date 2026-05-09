package com.tow.backend.newsletter.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * DTO para enviar una newsletter a todos los suscriptores.
 */
@Data
public class NewsletterBroadcastRequest {

    @NotBlank(message = "El asunto es obligatorio")
    private String subject;

    @NotBlank(message = "El contenido es obligatorio")
    private String content;
}
