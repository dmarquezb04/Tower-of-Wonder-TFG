package com.tow.backend.user.service;

import com.tow.backend.user.dto.TwoFactorSetupDTO;
import com.tow.backend.user.dto.UserProfileDTO;

import com.tow.backend.user.entity.User;
import com.tow.backend.user.repository.UserRepository;
import com.tow.backend.email.service.MailService;
import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final GoogleAuthenticator googleAuthenticator;
    private final MailService mailService;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    @Transactional(readOnly = true)
    public UserProfileDTO getUserProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
                
        return UserProfileDTO.builder()
                .idUsuario(user.getIdUsuario())
                .email(user.getEmail())
                .username(user.getUsername())
                .twoFaEnabled(user.getTwoFaEnabled())
                .activo(user.getActivo())
                .fechaCreacion(user.getFechaCreacion())
                .ultimoLogin(user.getUltimoLogin())
                .role(user.getRole().getNombreRol())
                .build();
    }

    public TwoFactorSetupDTO generateTwoFactorSetup(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (Boolean.TRUE.equals(user.getTwoFaEnabled())) {
            throw new RuntimeException("2FA ya está activado");
        }

        // Generate a new secret
        GoogleAuthenticatorKey key = googleAuthenticator.createCredentials();
        String secret = key.getKey();
        
        // Format the URI for QR code
        // otpauth://totp/TowerOfWonder:username?secret=XYZ&issuer=TowerOfWonder
        String issuer = "TowerOfWonder";
        String accountName = user.getUsername() != null && !user.getUsername().isEmpty() ? user.getUsername() : user.getEmail();
        String qrCodeUri = String.format("otpauth://totp/%s:%s?secret=%s&issuer=%s",
                issuer, accountName, secret, issuer);

        return TwoFactorSetupDTO.builder()
                .secret(secret)
                .qrCodeUri(qrCodeUri)
                .build();
    }

    @Transactional
    public void enableTwoFactor(String email, String secret, String code) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (Boolean.TRUE.equals(user.getTwoFaEnabled())) {
            throw new RuntimeException("2FA ya está activado");
        }

        // Verify the code against the provided secret
        int codeInt;
        try {
            codeInt = Integer.parseInt(code);
        } catch (NumberFormatException e) {
            throw new RuntimeException("El código debe ser numérico");
        }

        boolean isValid = googleAuthenticator.authorize(secret, codeInt);
        if (!isValid) {
            throw new RuntimeException("El código es incorrecto");
        }

        // Code is valid, save secret and enable 2FA
        user.setTwofaSecret(secret);
        user.setTwoFaEnabled(true);
        userRepository.save(user);
        log.info("2FA activado correctamente para el usuario: {}", email);
    }

    @Transactional
    public void disableTwoFactor(String email, String code) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (!Boolean.TRUE.equals(user.getTwoFaEnabled())) {
            throw new RuntimeException("2FA no está activado");
        }

        // Verify the code against the current secret to allow disabling
        int codeInt;
        try {
            codeInt = Integer.parseInt(code);
        } catch (NumberFormatException e) {
            throw new RuntimeException("El código debe ser numérico");
        }

        boolean isValid = googleAuthenticator.authorize(user.getTwofaSecret(), codeInt);
        if (!isValid) {
            throw new RuntimeException("El código es incorrecto");
        }

        user.setTwofaSecret(null);
        user.setTwoFaEnabled(false);
        userRepository.save(user);
        log.info("2FA desactivado para el usuario: {}", email);
    }

    @Transactional
    public void deleteAccount(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        user.setActivo(false);
        String token = UUID.randomUUID().toString();
        user.setRecoveryToken(token);
        user.setRecoveryTokenExpiry(LocalDateTime.now().plusDays(7));
        userRepository.save(user);
        log.info("Cuenta desactivada (borrado lógico). Token de recuperación generado para: {}", email);
        
        String reactivateLink = frontendUrl + "/reactivate?token=" + token;
        mailService.sendHtmlEmail(
            user.getEmail(),
            "Tu cuenta ha sido desactivada",
            "account_deactivation",
            Map.of(
                "username", user.getUsername() != null && !user.getUsername().isEmpty() ? user.getUsername() : "Usuario",
                "reactivationLink", reactivateLink
            )
        );
    }

    @Transactional
    public void reactivateAccount(String token) {
        User user = userRepository.findByRecoveryToken(token)
                .orElseThrow(() -> new RuntimeException("Enlace de recuperación inválido o expirado"));

        if (user.getRecoveryTokenExpiry() != null && user.getRecoveryTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("El enlace de recuperación ha caducado. Contacta con soporte.");
        }

        user.setActivo(true);
        user.setRecoveryToken(null);
        user.setRecoveryTokenExpiry(null);
        userRepository.save(user);
        log.info("Cuenta reactivada con éxito mediante token para el usuario: {}", user.getEmail());
    }
}
