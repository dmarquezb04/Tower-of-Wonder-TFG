package com.tow.backend.contact.service;

import com.tow.backend.contact.dto.ContactRequest;
import com.tow.backend.contact.entity.ContactMessage;
import com.tow.backend.contact.repository.ContactMessageRepository;
import com.tow.backend.email.service.MailService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * ImplementaciÃ³n del servicio de contacto.
 *
 * @see ContactService
 * @author DarÃ­o MÃ¡rquez Bautista
 */
@Service
@RequiredArgsConstructor
public class ContactServiceImpl implements ContactService {

    private final ContactMessageRepository contactMessageRepository;
    private final MailService mailService;

    @Value("${app.admin-email}")
    private String adminEmail;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    @Override
    @Transactional
    public void processContactMessage(ContactRequest request) {
        ContactMessage message = ContactMessage.builder()
                .nombre(request.getNombre())
                .email(request.getEmail())
                .asunto(request.getAsunto())
                .mensaje(request.getMensaje())
                .build();
        
        contactMessageRepository.save(message);

        mailService.sendHtmlEmail(
            adminEmail,
            "Nuevo mensaje de contacto: " + request.getAsunto(),
            "contact_notification",
            Map.of(
                "nombre", request.getNombre(),
                "email", request.getEmail(),
                "asunto", request.getAsunto(),
                "mensaje", request.getMensaje(),
                "baseUrl", frontendUrl
            )
        );
    }
}


