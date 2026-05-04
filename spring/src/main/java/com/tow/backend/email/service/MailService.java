package com.tow.backend.email.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class MailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${spring.mail.username:noreply.towerofwonder@gmail.com}")
    private String fromEmail;

    @Value("${spring.mail.properties.mail.from-name:Tower of Wonder}")
    private String fromName;

    /**
     * Envía un email HTML renderizado con Thymeleaf de forma asíncrona.
     *
     * @param to           Destinatario
     * @param subject      Asunto
     * @param templateName Nombre del archivo en src/main/resources/templates/email/ (sin .html)
     * @param variables    Variables para inyectar en la plantilla
     */
    @Async
    public void sendHtmlEmail(String to, String subject, String templateName, Map<String, Object> variables) {
        try {
            Context context = new Context();
            if (variables != null) {
                context.setVariables(variables);
            }

            String htmlContent = templateEngine.process("email/" + templateName, context);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            // Configurar el remitente con nombre "Tower of Wonder <correo@gmail.com>"
            helper.setFrom(String.format("%s <%s>", fromName, fromEmail));
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true); // true indica que es HTML

            mailSender.send(message);
            log.info("Email [{}] enviado correctamente a {}", subject, to);

        } catch (MessagingException e) {
            log.error("Error al enviar email a {}: {}", to, e.getMessage());
            throw new RuntimeException("No se pudo enviar el correo electrónico", e);
        }
    }
}
