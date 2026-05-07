package com.tow.backend.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO para la peticiÃ³n de login.
 * Recibido en {@code POST /api/auth/login}.
 */
@Getter
@Setter
public class LoginRequest {

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El email no tiene un formato vÃ¡lido")
    private String email;

    @NotBlank(message = "La contraseña es obligatoria")
    private String password;
}

