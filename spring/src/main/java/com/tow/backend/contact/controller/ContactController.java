package com.tow.backend.contact.controller;

import com.tow.backend.contact.dto.ContactRequest;
import com.tow.backend.contact.service.ContactService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/contacto")
@RequiredArgsConstructor
public class ContactController {

    private final ContactService contactService;

    @PostMapping
    public ResponseEntity<?> sendContactMessage(@Valid @RequestBody ContactRequest request) {
        contactService.processContactMessage(request);
        return ResponseEntity.ok(Map.of(
            "ok", true,
            "message", "¡Mensaje enviado correctamente! Te responderemos pronto."
        ));
    }
}
