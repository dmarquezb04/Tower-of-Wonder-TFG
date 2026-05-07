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
 * Controlador REST para la gestiÃ³n de suscripciones a la newsletter.
 *
 * <p>Implementa el flujo de doble opt-in:
 * <ol>
 *   <li>{@code POST /newsletter/subscribe} â€” solicitud de suscripciÃ³n, envÃ­a email de confirmaciÃ³n</li>
 *   <li>{@code POST /newsletter/confirm}   â€” confirmaciÃ³n mediante token recibido por email</li>
 * </ol>
 *
 * <p>Todos los endpoints son pÃºblicos.
 * Los errores de negocio son gestionados por {@link com.tow.backend.exception.GlobalExceptionHandler}.
 *
 * @author DarÃ­o MÃ¡rquez Bautista
 */
@RestController
@RequestMapping("/newsletter")
@RequiredArgsConstructor
@Tag(name = "Newsletter", description = "SuscripciÃ³n a la newsletter con doble opt-in")
public class NewsletterController {

    private final SubscriberRepository subscriberRepository;
    private final MailService mailService;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    /**
     * Registra una solicitud de suscripciÃ³n y envÃ­a un email de confirmaciÃ³n.
     *
     * <p>Si el email ya existe pero no estÃ¡ confirmado, reenvÃ­a el correo de confirmaciÃ³n.
     *
     * @param request body con el email a suscribir
     * @return 200 OK con instrucciones para confirmar la suscripciÃ³n
     */
    @PostMapping("/subscribe")
    @Operation(summary = "Suscribirse a la newsletter (paso 1 â€” envÃ­a email de confirmaciÃ³n)")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Email de confirmaciÃ³n enviado"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Email con formato invÃ¡lido"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "El email ya estÃ¡ suscrito y confirmado")
    })
    public ResponseEntity<ApiResponse> subscribe(@Valid @RequestBody SubscribeRequest request) {
        Optional<Subscriber> existing = subscriberRepository.findByEmail(request.getEmail());
        Subscriber subscriber;

        if (existing.isPresent()) {
            subscriber = existing.get();
            if (subscriber.getConfirmed()) {
                throw new ConflictException("Este correo ya estÃ¡ suscrito a la newsletter");
            }
            // Si no estÃ¡ confirmado, reenviamos el email de confirmaciÃ³n
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
                "Confirma tu suscripciÃ³n a la Newsletter",
                "newsletter_confirm",
                Map.of("email", request.getEmail(), "confirmationLink", confirmLink)
        );

        return ResponseEntity.ok(new ApiResponse("Te hemos enviado un correo para confirmar tu suscripciÃ³n."));
    }

    /**
     * Confirma la suscripciÃ³n a la newsletter mediante el token enviado por email.
     *
     * @param token token de confirmaciÃ³n recibido por email
     * @return 200 OK con mensaje de bienvenida
     */
    @PostMapping("/confirm")
    @Operation(summary = "Confirmar suscripciÃ³n con token (paso 2)")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "SuscripciÃ³n confirmada correctamente"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Token ausente o suscripciÃ³n ya confirmada"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Token invÃ¡lido o expirado")
    })
    public ResponseEntity<ApiResponse> confirm(@RequestParam String token) {
        if (!StringUtils.hasText(token)) {
            throw new BadRequestException("Falta el token de confirmaciÃ³n");
        }

        Subscriber subscriber = subscriberRepository.findByConfirmationToken(token)
                .orElseThrow(() -> new NotFoundException("Token de confirmaciÃ³n invÃ¡lido o expirado"));

        if (subscriber.getConfirmed()) {
            throw new BadRequestException("Esta suscripciÃ³n ya fue confirmada anteriormente");
        }

        subscriber.setConfirmed(true);
        subscriber.setConfirmationToken(null);
        subscriberRepository.save(subscriber);

        mailService.sendHtmlEmail(
                subscriber.getEmail(),
                "Â¡Bienvenido a la Newsletter!",
                "newsletter_welcome",
                Map.of("email", subscriber.getEmail())
        );

        return ResponseEntity.ok(new ApiResponse("SuscripciÃ³n confirmada correctamente. Â¡Bienvenido!"));
    }
}


