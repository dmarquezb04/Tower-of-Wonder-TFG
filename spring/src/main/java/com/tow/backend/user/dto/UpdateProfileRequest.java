package com.tow.backend.user.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para la actualizaciÃ³n del perfil del usuario autenticado.
 * Recibido en {@code PUT /user/profile}.
 *
 * <p>Todos los campos son opcionales. Solo se actualizarÃ¡n los campos
 * que se envÃ­en con valor no nulo y no vacÃ­o.
 *
 * @author DarÃ­o MÃ¡rquez Bautista
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProfileRequest {

    /** Nuevo nombre de usuario. Entre 3 y 50 caracteres alfanumÃ©ricos. */
    @Size(min = 3, max = 50, message = "El nombre de usuario debe tener entre 3 y 50 caracteres")
    @Pattern(
            regexp = "^[a-zA-Z0-9_]*$",
            message = "El nombre de usuario solo puede contener letras, nÃºmeros y guiones bajos"
    )
    private String username;

    /** ContraseÃ±a actual del usuario, requerida para cambiar la contraseÃ±a. */
    private String currentPassword;

    /** Nueva contraseÃ±a. MÃ­nimo 8 caracteres si se proporciona. */
    @Size(min = 8, max = 100, message = "La contraseÃ±a debe tener entre 8 y 100 caracteres")
    private String newPassword;
}

