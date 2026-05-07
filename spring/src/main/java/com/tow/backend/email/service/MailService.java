package com.tow.backend.email.service;

import java.util.Map;

/**
 * Contrato del servicio de mensajerÃ­a por correo electrÃ³nico.
 *
 * <p>Proporciona mÃ©todos para enviar correos electrÃ³nicos formateados en HTML
 * utilizando plantillas de Thymeleaf.
 *
 * @author DarÃ­o MÃ¡rquez Bautista
 */
public interface MailService {

    /**
     * EnvÃ­a un email HTML de forma asÃ­ncrona.
     *
     * <p>Utiliza plantillas alojadas en {@code src/main/resources/templates/email/}.
     *
     * @param to           destinatario del correo
     * @param subject      asunto del mensaje
     * @param templateName nombre de la plantilla Thymeleaf (sin extensiÃ³n .html)
     * @param variables    mapa de variables para inyectar en la plantilla
     */
    void sendHtmlEmail(String to, String subject, String templateName, Map<String, Object> variables);
}


