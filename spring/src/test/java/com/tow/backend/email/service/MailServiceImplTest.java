package com.tow.backend.email.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MailServiceImplTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private TemplateEngine templateEngine;

    @Mock
    private MimeMessage mimeMessage;

    @InjectMocks
    private MailServiceImpl mailService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(mailService, "fromEmail", "test@tow.com");
        ReflectionTestUtils.setField(mailService, "fromName", "Test App");
    }

    @Test
    void sendHtmlEmail_Success() {
        when(templateEngine.process(anyString(), any(Context.class))).thenReturn("<html>Test</html>");
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        Map<String, Object> variables = Map.of("key", "value");
        mailService.sendHtmlEmail("user@test.com", "Test Subject", "test_template", variables);

        verify(templateEngine).process(eq("email/test_template"), any(Context.class));
        
        ArgumentCaptor<MimeMessage> messageCaptor = ArgumentCaptor.forClass(MimeMessage.class);
        verify(mailSender).send(messageCaptor.capture());
        
        assertEquals(mimeMessage, messageCaptor.getValue());
    }

    @Test
    void sendHtmlEmail_MessagingException_DoesNotThrow() {
        when(templateEngine.process(anyString(), any(Context.class))).thenReturn("<html>Test</html>");
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        
        // Simular un error al configurar o enviar el MimeMessage
        doThrow(new org.springframework.mail.MailSendException("Simulated error"))
                .when(mailSender).send(any(MimeMessage.class));

        // El servicio usa un try-catch que atrapa excepciones de mensajería
        assertDoesNotThrow(() -> 
            mailService.sendHtmlEmail("user@test.com", "Subject", "template", null)
        );
    }
}
