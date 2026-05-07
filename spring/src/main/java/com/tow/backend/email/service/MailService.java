package com.tow.backend.email.service;

import java.util.Map;

/**
 * Contrato del servicio de mensajería por correo electrónico.
 *
 * <p>Proporciona métodos para enviar correos electrónicos formateados en HTML
 * utilizando plantillas de Thymeleaf.
 *
 * @author Darío Márquez Bautista
 */
public interface MailService {

    /**
     * Envía un email HTML de forma asíncrona.
     *
     * <p>Utiliza plantillas alojadas en {@code src/main/resources/templates/email/}.
     *
     * @param to           destinatario del correo
     * @param subject      asunto del mensaje
     * @param templateName nombre de la plantilla Thymeleaf (sin extensión .html)
     * @param variables    mapa de variables para inyectar en la plantilla
     */
    void sendHtmlEmail(String to, String subject, String templateName, Map<String, Object> variables);
}


