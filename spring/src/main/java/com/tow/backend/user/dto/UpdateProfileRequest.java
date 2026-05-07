package com.tow.backend.user.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para la actualización del perfil del usuario autenticado.
 * Recibido en {@code PUT /user/profile}.
 *
 * <p>Todos los campos son opcionales. Solo se actualizarán los campos
 * que se envíen con valor no nulo y no vacío.
 *
 * @author Darío Márquez Bautista
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProfileRequest {

    /** Nuevo nombre de usuario. Entre 3 y 50 caracteres alfanuméricos. */
    @Size(min = 3, max = 50, message = "El nombre de usuario debe tener entre 3 y 50 caracteres")
    @Pattern(
            regexp = "^[a-zA-Z0-9_]*$",
            message = "El nombre de usuario solo puede contener letras, números y guiones bajos"
    )
    private String username;

    /** Contraseña actual del usuario, requerida para cambiar la contraseña. */
    private String currentPassword;

    /** Nueva contraseña. Mínimo 8 caracteres si se proporciona. */
    @Size(min = 8, max = 100, message = "La contraseña debe tener entre 8 y 100 caracteres")
    private String newPassword;
}

