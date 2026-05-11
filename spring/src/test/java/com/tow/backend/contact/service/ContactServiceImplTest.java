package com.tow.backend.contact.service;

import com.tow.backend.contact.dto.ContactRequest;
import com.tow.backend.contact.entity.ContactMessage;
import com.tow.backend.contact.repository.ContactMessageRepository;
import com.tow.backend.email.service.MailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ContactServiceImplTest {

    @Mock
    private ContactMessageRepository contactMessageRepository;

    @Mock
    private MailService mailService;

    @InjectMocks
    private ContactServiceImpl contactService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(contactService, "adminEmail", "admin@tow.com");
        ReflectionTestUtils.setField(contactService, "frontendUrl", "http://localhost:3000");
    }

    @Test
    void processContactMessage_Success() {
        ContactRequest request = new ContactRequest();
        request.setNombre("Test User");
        request.setEmail("test@test.com");
        request.setAsunto("Consulta general");
        request.setMensaje("Tengo un problema con la página.");

        contactService.processContactMessage(request);

        ArgumentCaptor<ContactMessage> messageCaptor = ArgumentCaptor.forClass(ContactMessage.class);
        verify(contactMessageRepository).save(messageCaptor.capture());

        ContactMessage savedMessage = messageCaptor.getValue();
        assertEquals("Test User", savedMessage.getNombre());
        assertEquals("test@test.com", savedMessage.getEmail());
        assertEquals("Consulta general", savedMessage.getAsunto());

        ArgumentCaptor<Map<String, Object>> mapCaptor = ArgumentCaptor.forClass(Map.class);
        verify(mailService).sendHtmlEmail(
                eq("admin@tow.com"),
                eq("Nuevo mensaje de contacto: Consulta general"),
                eq("contact_notification"),
                mapCaptor.capture());

        Map<String, Object> templateParams = mapCaptor.getValue();
        assertEquals("Test User", templateParams.get("nombre"));
        assertEquals("test@test.com", templateParams.get("email"));
        assertTrue(templateParams.containsKey("baseUrl"));
    }
}
