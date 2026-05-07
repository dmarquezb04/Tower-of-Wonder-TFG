package com.tow.backend.user.service;

import com.tow.backend.email.service.MailService;
import com.tow.backend.exception.BadRequestException;
import com.tow.backend.exception.ConflictException;
import com.tow.backend.exception.NotFoundException;
import com.tow.backend.user.dto.TwoFactorSetupDTO;
import com.tow.backend.user.dto.UpdateProfileRequest;
import com.tow.backend.user.dto.UserProfileDTO;
import com.tow.backend.user.entity.User;
import com.tow.backend.user.repository.UserRepository;
import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * ImplementaciÃ³n del servicio de gestiÃ³n de perfil y cuenta de usuario.
 *
 * @see UserService
 * @author DarÃ­o MÃ¡rquez Bautista
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final GoogleAuthenticator googleAuthenticator;
    private final MailService mailService;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    @Override
    @Transactional(readOnly = true)
    public UserProfileDTO getUserProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));

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

    @Override
    public TwoFactorSetupDTO generateTwoFactorSetup(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));

        if (Boolean.TRUE.equals(user.getTwoFaEnabled())) {
            throw new ConflictException("El 2FA ya estÃ¡ activado para esta cuenta");
        }

        GoogleAuthenticatorKey key = googleAuthenticator.createCredentials();
        String secret = key.getKey();

        String issuer = "TowerOfWonder";
        String accountName = (user.getUsername() != null && !user.getUsername().isEmpty())
                ? user.getUsername()
                : user.getEmail();
        String qrCodeUri = String.format("otpauth://totp/%s:%s?secret=%s&issuer=%s",
                issuer, accountName, secret, issuer);

        return TwoFactorSetupDTO.builder()
                .secret(secret)
                .qrCodeUri(qrCodeUri)
                .build();
    }

    @Override
    @Transactional
    public void enableTwoFactor(String email, String secret, String code) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));

        if (Boolean.TRUE.equals(user.getTwoFaEnabled())) {
            throw new ConflictException("El 2FA ya estÃ¡ activado para esta cuenta");
        }

        int codeInt;
        try {
            codeInt = Integer.parseInt(code);
        } catch (NumberFormatException e) {
            throw new BadRequestException("El cÃ³digo de verificaciÃ³n debe ser numÃ©rico");
        }

        if (!googleAuthenticator.authorize(secret, codeInt)) {
            throw new BadRequestException("El cÃ³digo de verificaciÃ³n es incorrecto");
        }

        user.setTwofaSecret(secret);
        user.setTwoFaEnabled(true);
        userRepository.save(user);
        log.info("2FA activado correctamente para el usuario: {}", email);
    }

    @Override
    @Transactional
    public void disableTwoFactor(String email, String code) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));

        if (!Boolean.TRUE.equals(user.getTwoFaEnabled())) {
            throw new BadRequestException("El 2FA no estÃ¡ activo en esta cuenta");
        }

        int codeInt;
        try {
            codeInt = Integer.parseInt(code);
        } catch (NumberFormatException e) {
            throw new BadRequestException("El cÃ³digo de verificaciÃ³n debe ser numÃ©rico");
        }

        if (!googleAuthenticator.authorize(user.getTwofaSecret(), codeInt)) {
            throw new BadRequestException("El cÃ³digo de verificaciÃ³n es incorrecto");
        }

        user.setTwofaSecret(null);
        user.setTwoFaEnabled(false);
        userRepository.save(user);
        log.info("2FA desactivado para el usuario: {}", email);
    }

    @Override
    @Transactional
    public void deleteAccount(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));

        user.setActivo(false);
        String token = UUID.randomUUID().toString();
        user.setRecoveryToken(token);
        user.setRecoveryTokenExpiry(LocalDateTime.now().plusDays(7));
        userRepository.save(user);
        log.info("Cuenta desactivada (borrado lÃ³gico). Token de recuperaciÃ³n generado para: {}", email);

        String reactivateLink = frontendUrl + "/reactivate?token=" + token;
        mailService.sendHtmlEmail(
                user.getEmail(),
                "Tu cuenta ha sido desactivada",
                "account_deactivation",
                Map.of(
                        "username", (user.getUsername() != null && !user.getUsername().isEmpty())
                                ? user.getUsername() : "Usuario",
                        "reactivationLink", reactivateLink
                )
        );
    }

    @Override
    @Transactional
    public void reactivateAccount(String token) {
        User user = userRepository.findByRecoveryToken(token)
                .orElseThrow(() -> new NotFoundException("Enlace de recuperaciÃ³n invÃ¡lido o expirado"));

        if (user.getRecoveryTokenExpiry() != null
                && user.getRecoveryTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("El enlace de recuperaciÃ³n ha caducado. Contacta con soporte.");
        }

        user.setActivo(true);
        user.setRecoveryToken(null);
        user.setRecoveryTokenExpiry(null);
        userRepository.save(user);
        log.info("Cuenta reactivada con Ã©xito para el usuario: {}", user.getEmail());
    }

    @Override
    @Transactional
    public void updateProfile(String currentEmail, UpdateProfileRequest request) {
        User user = userRepository.findByEmail(currentEmail)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));

        boolean usernameChanged = false;
        boolean passwordChanged = false;

        if (request.getUsername() != null && !request.getUsername().trim().isEmpty()
                && !request.getUsername().equals(user.getUsername())) {
            user.setUsername(request.getUsername());
            usernameChanged = true;
        }

        if (request.getNewPassword() != null && !request.getNewPassword().trim().isEmpty()) {
            if (request.getCurrentPassword() == null || request.getCurrentPassword().isEmpty()) {
                throw new BadRequestException("Debes introducir tu contraseÃ±a actual para cambiarla");
            }
            if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
                throw new BadRequestException("La contraseÃ±a actual es incorrecta");
            }
            user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
            passwordChanged = true;
        }

        if (usernameChanged || passwordChanged) {
            userRepository.save(user);
            log.info("Perfil actualizado para {}: usernameChanged={}, passwordChanged={}",
                    currentEmail, usernameChanged, passwordChanged);

            StringBuilder changes = new StringBuilder();
            if (usernameChanged) changes.append("Nombre de usuario");
            if (usernameChanged && passwordChanged) changes.append(" y ");
            if (passwordChanged) changes.append("ContraseÃ±a");

            mailService.sendHtmlEmail(
                    user.getEmail(),
                    "Seguridad: Cambios en tu cuenta",
                    "credential_update",
                    Map.of(
                            "username", user.getUsername() != null ? user.getUsername() : "Usuario",
                            "changedFields", changes.toString()
                    )
            );
        }
    }
}


