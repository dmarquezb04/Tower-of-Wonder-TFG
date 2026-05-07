package com.tow.backend.newsletter.controller;

import com.tow.backend.common.dto.ApiResponse;
import com.tow.backend.email.service.MailService;
import com.tow.backend.exception.BadRequestException;
import com.tow.backend.exception.ConflictException;
import com.tow.backend.exception.NotFoundException;
import com.tow.backend.newsletter.dto.SubscribeRequest;
import com.tow.backend.newsletter.entity.Subscriber;
import com.tow.backend.newsletter.repository.SubscriberRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Controlador REST para la gestión de suscripciones a la newsletter.
 *
 * <p>Implementa el flujo de doble opt-in:
 * <ol>
 *   <li>{@code POST /newsletter/subscribe} — solicitud de suscripción, envía email de confirmación</li>
 *   <li>{@code POST /newsletter/confirm}   — confirmación mediante token recibido por email</li>
 * </ol>
 *
 * <p>Todos los endpoints son públicos.
 * Los errores de negocio son gestionados por {@link com.tow.backend.exception.GlobalExceptionHandler}.
 *
 * @author Darío Márquez Bautista
 */
@RestController
@RequestMapping("/newsletter")
@RequiredArgsConstructor
@Tag(name = "Newsletter", description = "Suscripción a la newsletter con doble opt-in")
public class NewsletterController {

    private final SubscriberRepository subscriberRepository;
    private final MailService mailService;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    /**
     * Registra una solicitud de suscripción y envía un email de confirmación.
     *
     * <p>Si el email ya existe pero no está confirmado, reenvía el correo de confirmación.
     *
     * @param request body con el email a suscribir
     * @return 200 OK con instrucciones para confirmar la suscripción
     */
    @PostMapping("/subscribe")
    @Operation(summary = "Suscribirse a la newsletter (paso 1 — envía email de confirmación)")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Email de confirmación enviado"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Email con formato inválido"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "El email ya está suscrito y confirmado")
    })
    public ResponseEntity<ApiResponse> subscribe(@Valid @RequestBody SubscribeRequest request) {
        Optional<Subscriber> existing = subscriberRepository.findByEmail(request.getEmail());
        Subscriber subscriber;

        if (existing.isPresent()) {
            subscriber = existing.get();
            if (subscriber.getConfirmed()) {
                throw new ConflictException("Este correo ya está suscrito a la newsletter");
            }
            // Si no está confirmado, reenviamos el email de confirmación
        } else {
            subscriber = new Subscriber();
            subscriber.setEmail(request.getEmail());
        }

        String token = UUID.randomUUID().toString();
        subscriber.setConfirmationToken(token);
        subscriber.setConfirmed(false);
        subscriberRepository.save(subscriber);

        String confirmLink = frontendUrl + "/newsletter/confirm?token=" + token;
        mailService.sendHtmlEmail(
                request.getEmail(),
                "Confirma tu suscripción a la Newsletter",
                "newsletter_confirm",
                Map.of("email", request.getEmail(), "confirmationLink", confirmLink)
        );

        return ResponseEntity.ok(new ApiResponse("Te hemos enviado un correo para confirmar tu suscripción."));
    }

    /**
     * Confirma la suscripción a la newsletter mediante el token enviado por email.
     *
     * @param token token de confirmación recibido por email
     * @return 200 OK con mensaje de bienvenida
     */
    @PostMapping("/confirm")
    @Operation(summary = "Confirmar suscripción con token (paso 2)")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Suscripción confirmada correctamente"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Token ausente o suscripción ya confirmada"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Token inválido o expirado")
    })
    public ResponseEntity<ApiResponse> confirm(@RequestParam String token) {
        if (!StringUtils.hasText(token)) {
            throw new BadRequestException("Falta el token de confirmación");
        }

        Subscriber subscriber = subscriberRepository.findByConfirmationToken(token)
                .orElseThrow(() -> new NotFoundException("Token de confirmación inválido o expirado"));

        if (subscriber.getConfirmed()) {
            throw new BadRequestException("Esta suscripción ya fue confirmada anteriormente");
        }

        subscriber.setConfirmed(true);
        subscriber.setConfirmationToken(null);
        subscriberRepository.save(subscriber);

        mailService.sendHtmlEmail(
                subscriber.getEmail(),
                "¡Bienvenido a la Newsletter!",
                "newsletter_welcome",
                Map.of("email", subscriber.getEmail())
        );

        return ResponseEntity.ok(new ApiResponse("Suscripción confirmada correctamente. ¡Bienvenido!"));
    }
}


