package com.tow.backend.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class TwoFactorCodeDTO {
    @NotBlank
    private String code;
    
    // Optional secret if it's sent from the client during setup
    private String secret;
}
