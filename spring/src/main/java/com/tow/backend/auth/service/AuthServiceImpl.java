package com.tow.backend.auth.service;

import com.tow.backend.auth.dto.LoginRequest;
import com.tow.backend.auth.dto.LoginResponse;
import com.tow.backend.auth.dto.RegisterRequest;
import com.tow.backend.auth.dto.TwoFactorRequest;
import com.tow.backend.exception.BadRequestException;
import com.tow.backend.exception.ConflictException;
import com.tow.backend.exception.NotFoundException;
import com.tow.backend.exception.UnauthorizedException;
import com.tow.backend.security.JwtTokenProvider;
import com.tow.backend.user.entity.Role;
import com.tow.backend.user.entity.User;
import com.tow.backend.user.repository.RoleRepository;
import com.tow.backend.user.repository.UserRepository;
import com.warrenstrange.googleauth.GoogleAuthenticator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * ImplementaciÃ³n del servicio de autenticaciÃ³n.
 *
 * @see AuthService
 * @author DarÃ­o MÃ¡rquez Bautista
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final JwtTokenProvider tokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final GoogleAuthenticator googleAuthenticator;

    // ============================================================
    // LOGIN
    // ============================================================

    @Override
    @Transactional
    public LoginResponse login(LoginRequest request) {
        // Mensaje genÃ©rico por seguridad â€” no revelar si el email existe en el sistema
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadCredentialsException("Credenciales incorrectas"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            log.warn("Intento de login fallido para: {}", request.getEmail());
            throw new BadCredentialsException("Credenciales incorrectas");
        }

        if (!Boolean.TRUE.equals(user.getActivo())) {
            throw new BadCredentialsException("La cuenta estÃ¡ desactivada");
        }

        List<String> roles = List.of(user.getRole().getNombreRol());

        // Con 2FA activo â†’ token temporal (5 min), el cliente debe completar el flujo
        if (Boolean.TRUE.equals(user.getTwoFaEnabled())) {
            String tempToken = tokenProvider.generateTwoFactorPendingToken(
                    user.getIdUsuario(), user.getEmail()
            );
            log.debug("Login con 2FA requerido para: {}", user.getEmail());
            return LoginResponse.builder()
                    .requiresTwoFactor(true)
                    .token(tempToken)
                    .email(user.getEmail())
                    .username(user.getUsername())
                    .message("Introduce el cÃ³digo de Google Authenticator")
                    .build();
        }

        // Sin 2FA â†’ token completo (24h)
        String token = tokenProvider.generateToken(user.getIdUsuario(), user.getEmail(), roles);
        actualizarUltimoLogin(user);

        log.info("Login exitoso para: {}", user.getEmail());
        return LoginResponse.builder()
                .requiresTwoFactor(false)
                .token(token)
                .email(user.getEmail())
                .username(user.getUsername())
                .message("Login exitoso")
                .build();
    }

    // ============================================================
    // VERIFY 2FA
    // ============================================================

    @Override
    @Transactional
    public LoginResponse verifyTwoFactor(String tokenTemporal, TwoFactorRequest request) {
        if (!tokenProvider.validateToken(tokenTemporal)
                || !tokenProvider.isTwoFactorPending(tokenTemporal)) {
            throw new UnauthorizedException("Token de verificaciÃ³n invÃ¡lido o expirado");
        }

        String email = tokenProvider.getEmailFromToken(tokenTemporal);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));

        int code;
        try {
            code = Integer.parseInt(request.getCode());
        } catch (NumberFormatException e) {
            throw new BadRequestException("El cÃ³digo de verificaciÃ³n debe ser numÃ©rico");
        }

        if (!googleAuthenticator.authorize(user.getTwofaSecret(), code)) {
            log.warn("CÃ³digo 2FA incorrecto para: {}", email);
            throw new UnauthorizedException("CÃ³digo de verificaciÃ³n incorrecto");
        }

        tokenProvider.revokeToken(tokenTemporal);

        List<String> roles = List.of(user.getRole().getNombreRol());
        String token = tokenProvider.generateToken(user.getIdUsuario(), user.getEmail(), roles);
        actualizarUltimoLogin(user);

        log.info("2FA verificado correctamente para: {}", email);
        return LoginResponse.builder()
                .requiresTwoFactor(false)
                .token(token)
                .email(user.getEmail())
                .username(user.getUsername())
                .message("AutenticaciÃ³n completada")
                .build();
    }

    // ============================================================
    // REGISTRO
    // ============================================================

    @Override
    @Transactional
    public void register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("El email ya estÃ¡ registrado");
        }
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new ConflictException("El nombre de usuario ya estÃ¡ en uso");
        }

        Role defaultRole = roleRepository.findByNombreRol("user")
                .orElseThrow(() -> new IllegalStateException(
                        "Rol 'user' no encontrado en la BD. Verificar datos iniciales."
                ));

        User newUser = new User();
        newUser.setEmail(request.getEmail());
        newUser.setUsername(request.getUsername());
        newUser.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        newUser.setActivo(true);
        newUser.setTwoFaEnabled(false);
        newUser.setRole(defaultRole);

        userRepository.save(newUser);
        log.info("Nuevo usuario registrado: {}", request.getEmail());
    }

    // ============================================================
    // LOGOUT
    // ============================================================

    @Override
    public void logout(String token) {
        tokenProvider.revokeToken(token);
        log.info("Logout: token revocado");
    }

    // ============================================================
    // MÃ©todos privados
    // ============================================================

    private void actualizarUltimoLogin(User user) {
        user.setUltimoLogin(LocalDateTime.now());
        userRepository.save(user);
    }
}


