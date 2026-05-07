package com.tow.backend.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO para la peticiÃ³n de registro de nuevo usuario.
 * Recibido en {@code POST /api/auth/register}.
 */
@Getter
@Setter
public class RegisterRequest {

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El email no tiene un formato vÃ¡lido")
    private String email;

    @NotBlank(message = "El nombre de usuario es obligatorio")
    @Size(min = 3, max = 50, message = "El nombre de usuario debe tener entre 3 y 50 caracteres")
    @Pattern(
        regexp = "^[a-zA-Z0-9_]+$",
        message = "El nombre de usuario solo puede contener letras, nÃºmeros y guiones bajos"
    )
    private String username;

    @NotBlank(message = "La contraseÃ±a es obligatoria")
    @Size(min = 8, max = 100, message = "La contraseÃ±a debe tener entre 8 y 100 caracteres")
    private String password;
}

