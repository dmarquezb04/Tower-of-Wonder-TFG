package com.tow.backend.auth.controller;

import com.tow.backend.auth.dto.LoginRequest;
import com.tow.backend.auth.dto.LoginResponse;
import com.tow.backend.auth.dto.RegisterRequest;
import com.tow.backend.auth.dto.TwoFactorRequest;
import com.tow.backend.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controlador REST para autenticación y registro.
 *
 * <p>Endpoints:
 * <ul>
 *   <li>{@code POST /api/auth/login}      — iniciar sesión</li>
 *   <li>{@code POST /api/auth/verify-2fa} — completar 2FA</li>
 *   <li>{@code POST /api/auth/register}   — registrar nuevo usuario</li>
 *   <li>{@code POST /api/auth/logout}     — cerrar sesión (revocar token)</li>
 * </ul>
 *
 * <p>Todos los endpoints son públicos (no requieren autenticación previa),
 * excepto {@code /logout} que requiere un token válido.
 *
 * @author Darío Márquez Bautista
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticación", description = "Login, registro, 2FA y logout")
public class AuthController {

    private final AuthService authService;

    /**
     * Inicia sesión con email y contraseña.
     *
     * <p>Responde con un token JWT. Si el usuario tiene 2FA activo,
     * el token es temporal (5min) y {@code requiresTwoFactor} es true.
     * En ese caso, el cliente debe llamar a {@code /verify-2fa}.
     *
     * @param request body con email y password
     * @return 200 OK con {@link LoginResponse}
     */
    @PostMapping("/login")
    @Operation(summary = "Iniciar sesión")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        try {
            LoginResponse response = authService.login(request);
            return ResponseEntity.ok(response);
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Verifica el código TOTP de Google Authenticator para completar el login 2FA.
     *
     * <p>Requiere el token temporal de 2FA en el header Authorization.
     *
     * @param authHeader header Authorization con el token temporal
     * @param request    body con el código TOTP de 6 dígitos
     * @return 200 OK con el token completo
     */
    @PostMapping("/verify-2fa")
    @Operation(summary = "Verificar código 2FA (Google Authenticator)")
    public ResponseEntity<?> verifyTwoFactor(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody TwoFactorRequest request
    ) {
        try {
            String token = extractBearerToken(authHeader);
            LoginResponse response = authService.verifyTwoFactor(token, request);
            return ResponseEntity.ok(response);
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Registra un nuevo usuario.
     *
     * @param request body con email, username y password
     * @return 201 Created si el registro fue exitoso
     */
    @PostMapping("/register")
    @Operation(summary = "Registrar nuevo usuario")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        try {
            authService.register(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of("message", "Usuario registrado correctamente"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Cierra la sesión revocando el token JWT.
     *
     * <p>Requiere un token válido en el header Authorization.
     * Tras el logout, el token queda en la blacklist y ya no puede usarse.
     *
     * @param authHeader header Authorization con el token a revocar
     * @return 200 OK
     */
    @PostMapping("/logout")
    @Operation(summary = "Cerrar sesión (revocar token JWT)")
    public ResponseEntity<?> logout(@RequestHeader("Authorization") String authHeader) {
        String token = extractBearerToken(authHeader);
        authService.logout(token);
        return ResponseEntity.ok(Map.of("message", "Sesión cerrada correctamente"));
    }

    // ============================================================
    // Métodos privados
    // ============================================================

    private String extractBearerToken(String authHeader) {
        if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        throw new IllegalArgumentException("Token de autorización no válido o ausente");
    }
}
