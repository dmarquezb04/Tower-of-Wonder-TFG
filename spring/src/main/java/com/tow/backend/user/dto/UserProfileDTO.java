package com.tow.backend.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileDTO {
    private Long idUsuario;
    private String email;
    private String username;
    private Boolean twoFaEnabled;
    private Boolean activo;
    private LocalDateTime fechaCreacion;
    private LocalDateTime ultimoLogin;
    private String role;
}
