package com.tow.backend.user.controller;

import com.tow.backend.user.dto.TwoFactorCodeDTO;
import com.tow.backend.user.dto.TwoFactorSetupDTO;
import com.tow.backend.user.dto.UserProfileDTO;
import com.tow.backend.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/profile")
    public ResponseEntity<UserProfileDTO> getProfile(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(userService.getUserProfile(userDetails.getUsername()));
    }

    @GetMapping("/2fa/setup")
    public ResponseEntity<TwoFactorSetupDTO> setupTwoFactor(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(userService.generateTwoFactorSetup(userDetails.getUsername()));
    }

    @PostMapping("/2fa/enable")
    public ResponseEntity<?> enableTwoFactor(@AuthenticationPrincipal UserDetails userDetails,
                                             @Valid @RequestBody TwoFactorCodeDTO request) {
        try {
            userService.enableTwoFactor(userDetails.getUsername(), request.getSecret(), request.getCode());
            return ResponseEntity.ok().body("{\"message\": \"2FA activado correctamente\"}");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }

    @PostMapping("/2fa/disable")
    public ResponseEntity<?> disableTwoFactor(@AuthenticationPrincipal UserDetails userDetails,
                                              @Valid @RequestBody TwoFactorCodeDTO request) {
        try {
            userService.disableTwoFactor(userDetails.getUsername(), request.getCode());
            return ResponseEntity.ok().body("{\"message\": \"2FA desactivado correctamente\"}");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }

    @DeleteMapping("/me")
    public ResponseEntity<?> deleteMe(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            userService.deleteAccount(userDetails.getUsername());
            return ResponseEntity.ok().body("{\"message\": \"Cuenta desactivada correctamente\"}");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }
}
