package com.tow.backend.newsletter.controller;

import com.tow.backend.email.service.MailService;
import com.tow.backend.newsletter.entity.Subscriber;
import com.tow.backend.newsletter.repository.SubscriberRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/newsletter")
@RequiredArgsConstructor
@Tag(name = "Newsletter", description = "Suscripción a la newsletter con doble opt-in")
public class NewsletterController {

    private final SubscriberRepository subscriberRepository;
    private final MailService mailService;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    @PostMapping("/subscribe")
    @Operation(summary = "Suscribirse a la newsletter (paso 1)")
    public ResponseEntity<?> subscribe(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        if (email == null || !email.contains("@")) {
            return ResponseEntity.badRequest().body(Map.of("error", "Email inválido"));
        }

        Optional<Subscriber> existing = subscriberRepository.findByEmail(email);
        Subscriber subscriber;
        
        if (existing.isPresent()) {
            subscriber = existing.get();
            if (subscriber.getConfirmed()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Este correo ya está suscrito"));
            }
            // Si no está confirmado, reenviamos el correo. 
            // En un caso real se podría poner un límite de tiempo para no hacer spam.
        } else {
            subscriber = new Subscriber();
            subscriber.setEmail(email);
        }

        String token = UUID.randomUUID().toString();
        subscriber.setConfirmationToken(token);
        subscriber.setConfirmed(false);
        subscriberRepository.save(subscriber);

        String confirmLink = frontendUrl + "/newsletter/confirm?token=" + token;
        mailService.sendHtmlEmail(
            email,
            "Confirma tu suscripción a la Newsletter",
            "newsletter_confirm",
            Map.of("email", email, "confirmationLink", confirmLink)
        );

        return ResponseEntity.ok(Map.of("message", "Te hemos enviado un correo para confirmar tu suscripción."));
    }

    @PostMapping("/confirm")
    @Operation(summary = "Confirmar suscripción con token (paso 2)")
    public ResponseEntity<?> confirm(@RequestBody Map<String, String> body) {
        String token = body.get("token");
        if (token == null || token.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Falta el token"));
        }

        Subscriber subscriber = subscriberRepository.findByConfirmationToken(token)
                .orElseThrow(() -> new RuntimeException("Token inválido o expirado"));

        if (subscriber.getConfirmed()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Esta suscripción ya fue confirmada"));
        }

        subscriber.setConfirmed(true);
        subscriber.setConfirmationToken(null);
        subscriberRepository.save(subscriber);

        // Enviar correo de bienvenida
        mailService.sendHtmlEmail(
            subscriber.getEmail(),
            "¡Bienvenido a la Newsletter!",
            "newsletter_welcome",
            Map.of("email", subscriber.getEmail())
        );

        return ResponseEntity.ok(Map.of("message", "Suscripción confirmada correctamente."));
    }
}
