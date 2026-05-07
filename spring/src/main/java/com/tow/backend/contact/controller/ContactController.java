package com.tow.backend.contact.controller;

import com.tow.backend.common.dto.ApiResponse;
import com.tow.backend.contact.dto.ContactRequest;
import com.tow.backend.contact.service.ContactService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST para el formulario de contacto.
 *
 * <p>Endpoint público que permite a cualquier visitante enviar un mensaje
 * al equipo de Tower of Wonder.
 * Los errores de negocio son gestionados por {@link com.tow.backend.exception.GlobalExceptionHandler}.
 *
 * @author Darío Márquez Bautista
 */
@RestController
@RequestMapping("/contacto")
@RequiredArgsConstructor
@Tag(name = "Contacto", description = "Formulario de contacto público")
public class ContactController {

    private final ContactService contactService;

    /**
     * Procesa y envía un mensaje del formulario de contacto.
     *
     * @param request body con nombre, email, asunto y mensaje
     * @return 200 OK con mensaje de confirmación
     */
    @PostMapping
    @Operation(summary = "Enviar mensaje de contacto")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Mensaje enviado correctamente"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Campos obligatorios ausentes o inválidos")
    })
    public ResponseEntity<ApiResponse> sendContactMessage(@Valid @RequestBody ContactRequest request) {
        contactService.processContactMessage(request);
        return ResponseEntity.ok(new ApiResponse("¡Mensaje enviado correctamente! Te responderemos pronto."));
    }
}


