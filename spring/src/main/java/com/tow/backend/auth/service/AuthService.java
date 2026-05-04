package com.tow.backend.auth.service;

import com.tow.backend.auth.dto.LoginRequest;
import com.tow.backend.auth.dto.LoginResponse;
import com.tow.backend.auth.dto.RegisterRequest;
import com.tow.backend.auth.dto.TwoFactorRequest;
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
import java.util.stream.Collectors;

/**
 * Servicio de autenticación. Contiene la lógica de negocio para login,
 * registro, verificación de 2FA y logout.
 *
 * <p>Equivale a {@code AuthService.php} y {@code AuthController.php}
 * del backend PHP legacy.
 *
 * @author Darío Márquez Bautista
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final JwtTokenProvider tokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final GoogleAuthenticator googleAuthenticator;

    // ============================================================
    // LOGIN
    // ============================================================

    /**
     * Procesa el intento de login.
     *
     * <p>Flujo:
     * <ol>
     *   <li>Buscar usuario por email</li>
     *   <li>Verificar contraseña con BCrypt</li>
     *   <li>Si el usuario tiene 2FA activo → devolver token temporal (5min)</li>
     *   <li>Si no tiene 2FA → devolver token completo (24h)</li>
     * </ol>
     *
     * @param request DTO con email y contraseña
     * @return respuesta con el token y si requiere 2FA
     * @throws BadCredentialsException si las credenciales son incorrectas
     */
    @Transactional
    public LoginResponse login(LoginRequest request) {
        // 1. Buscar usuario (mensaje genérico por seguridad — no revelar si el email existe)
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadCredentialsException("Credenciales incorrectas"));

        // 2. Verificar contraseña
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            log.warn("Intento de login fallido para: {}", request.getEmail());
            throw new BadCredentialsException("Credenciales incorrectas");
        }

        // 3. Verificar que la cuenta está activa
        if (!Boolean.TRUE.equals(user.getActivo())) {
            throw new BadCredentialsException("La cuenta está desactivada");
        }

        List<String> roles = List.of(user.getRole().getNombreRol());

        // 4a. Si tiene 2FA → token temporal, el cliente debe completar el flujo
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
                    .message("Introduce el código de Google Authenticator")
                    .build();
        }

        // 4b. Sin 2FA → token completo
        String token = tokenProvider.generateToken(
                user.getIdUsuario(), user.getEmail(), roles
        );
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

    /**
     * Verifica el código TOTP y completa el login cuando el usuario tiene 2FA activo.
     *
     * <p>El cliente debe incluir el token temporal en el header Authorization.
     *
     * @param tokenTemporal token temporal de 2FA (del header Authorization)
     * @param request       DTO con el código TOTP de 6 dígitos
     * @return token completo si el código es válido
     * @throws BadCredentialsException si el código es incorrecto o el token es inválido
     */
    @Transactional
    public LoginResponse verifyTwoFactor(String tokenTemporal, TwoFactorRequest request) {
        // Validar que el token es temporal de 2FA
        if (!tokenProvider.validateToken(tokenTemporal)
                || !tokenProvider.isTwoFactorPending(tokenTemporal)) {
            throw new BadCredentialsException("Token de verificación inválido o expirado");
        }

        String email = tokenProvider.getEmailFromToken(tokenTemporal);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadCredentialsException("Usuario no encontrado"));

        // Verificar código TOTP con la librería googleauth (misma lógica que PHP)
        int code = Integer.parseInt(request.getCode());
        boolean valid = googleAuthenticator.authorize(user.getTwofaSecret(), code);

        if (!valid) {
            log.warn("Código 2FA incorrecto para: {}", email);
            throw new BadCredentialsException("Código de verificación incorrecto");
        }

        // Revocar el token temporal (ya no es necesario)
        tokenProvider.revokeToken(tokenTemporal);

        // Emitir token completo
        List<String> roles = List.of(user.getRole().getNombreRol());
        String token = tokenProvider.generateToken(user.getIdUsuario(), user.getEmail(), roles);
        actualizarUltimoLogin(user);

        log.info("2FA verificado correctamente para: {}", email);
        return LoginResponse.builder()
                .requiresTwoFactor(false)
                .token(token)
                .email(user.getEmail())
                .username(user.getUsername())
                .message("Autenticación completada")
                .build();
    }

    // ============================================================
    // REGISTRO
    // ============================================================

    /**
     * Registra un nuevo usuario en el sistema.
     *
     * @param request DTO con email, username y contraseña
     * @throws IllegalArgumentException si el email o username ya están en uso
     */
    @Transactional
    public void register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("El email ya está registrado");
        }
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("El nombre de usuario ya está en uso");
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

    /**
     * Revoca el token JWT del usuario, efectuando un logout real.
     *
     * @param token token JWT a revocar (sin el prefijo "Bearer ")
     */
    public void logout(String token) {
        tokenProvider.revokeToken(token);
        log.info("Logout: token revocado");
    }

    // ============================================================
    // Métodos privados
    // ============================================================

    private List<String> extractRoleNames(User user) {
        return List.of(user.getRole().getNombreRol());
    }

    private void actualizarUltimoLogin(User user) {
        user.setUltimoLogin(LocalDateTime.now());
        userRepository.save(user);
    }
}
