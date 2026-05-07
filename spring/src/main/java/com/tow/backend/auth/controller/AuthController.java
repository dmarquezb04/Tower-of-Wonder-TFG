package com.tow.backend.auth.controller;

import com.tow.backend.auth.dto.LoginRequest;
import com.tow.backend.auth.dto.LoginResponse;
import com.tow.backend.auth.dto.RegisterRequest;
import com.tow.backend.auth.dto.TwoFactorRequest;
import com.tow.backend.auth.service.AuthService;
import com.tow.backend.common.dto.ApiResponse;
import com.tow.backend.exception.BadRequestException;
import com.tow.backend.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST para autenticación y registro.
 *
 * <p>Endpoints:
 * <ul>
 *   <li>{@code POST /auth/login}      — iniciar sesión</li>
 *   <li>{@code POST /auth/verify-2fa} — completar 2FA</li>
 *   <li>{@code POST /auth/register}   — registrar nuevo usuario</li>
 *   <li>{@code POST /auth/logout}     — cerrar sesión (revocar token)</li>
 *   <li>{@code POST /auth/reactivate} — reactivar cuenta con borrado lógico</li>
 * </ul>
 *
 * <p>Todos los endpoints son públicos excepto {@code /logout}, que requiere un token válido.
 * Los errores de negocio son gestionados por {@link com.tow.backend.exception.GlobalExceptionHandler}.
 *
 * @author Darío Márquez Bautista
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticación", description = "Login, registro, 2FA y logout")
public class AuthController {

    private final AuthService authService;
    private final UserService userService;

    /**
     * Inicia sesión con email y contraseña.
     *
     * <p>Si el usuario tiene 2FA activo, el token devuelto es temporal (5 min) y
     * {@code requiresTwoFactor} es {@code true}. En ese caso, el cliente debe
     * llamar a {@code /verify-2fa} con el código TOTP.
     *
     * @param request body con email y password
     * @return 200 OK con {@link LoginResponse}
     */
    @PostMapping("/login")
    @Operation(summary = "Iniciar sesión")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Login correcto"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Campos inválidos"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Credenciales incorrectas o cuenta desactivada")
    })
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    /**
     * Verifica el código TOTP de Google Authenticator para completar el login 2FA.
     *
     * <p>Requiere el token temporal de 2FA en el header {@code Authorization}.
     *
     * @param authHeader header Authorization con el token temporal
     * @param request    body con el código TOTP de 6 dígitos
     * @return 200 OK con el token completo
     */
    @PostMapping("/verify-2fa")
    @Operation(summary = "Verificar código 2FA (Google Authenticator)")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "2FA verificado correctamente"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Código con formato inválido"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Token temporal inválido o código incorrecto")
    })
    public ResponseEntity<LoginResponse> verifyTwoFactor(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody TwoFactorRequest request
    ) {
        String token = extractBearerToken(authHeader);
        return ResponseEntity.ok(authService.verifyTwoFactor(token, request));
    }

    /**
     * Registra un nuevo usuario en el sistema.
     *
     * @param request body con email, username y password
     * @return 201 Created con mensaje de confirmación
     */
    @PostMapping("/register")
    @Operation(summary = "Registrar nuevo usuario")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Usuario registrado correctamente"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Campos inválidos"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "El email o nombre de usuario ya están en uso")
    })
    public ResponseEntity<ApiResponse> register(@Valid @RequestBody RegisterRequest request) {
        authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse("Usuario registrado correctamente"));
    }

    /**
     * Cierra la sesión revocando el token JWT.
     *
     * <p>Tras el logout, el token queda en la blacklist y ya no puede usarse,
     * aunque no haya expirado aún.
     *
     * @param authHeader header Authorization con el token a revocar
     * @return 200 OK con mensaje de confirmación
     */
    @PostMapping("/logout")
    @Operation(summary = "Cerrar sesión (revocar token JWT)")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Sesión cerrada correctamente"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Header Authorization ausente o inválido")
    })
    public ResponseEntity<ApiResponse> logout(@RequestHeader("Authorization") String authHeader) {
        String token = extractBearerToken(authHeader);
        authService.logout(token);
        return ResponseEntity.ok(new ApiResponse("Sesión cerrada correctamente"));
    }

    /**
     * Reactiva una cuenta que fue desactivada mediante borrado lógico.
     *
     * @param token token de recuperación recibido por email
     * @return 200 OK con mensaje de confirmación
     */
    @PostMapping("/reactivate")
    @Operation(summary = "Reactivar cuenta desactivada")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Cuenta reactivada correctamente"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Token ausente, inválido o caducado"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Token no corresponde a ninguna cuenta")
    })
    public ResponseEntity<ApiResponse> reactivateAccount(@RequestParam String token) {
        if (!StringUtils.hasText(token)) {
            throw new BadRequestException("Falta el token de recuperación");
        }
        userService.reactivateAccount(token);
        return ResponseEntity.ok(new ApiResponse("Cuenta reactivada correctamente. Ya puedes iniciar sesión."));
    }

    // ============================================================
    // Métodos privados
    // ============================================================

    private String extractBearerToken(String authHeader) {
        if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        throw new BadRequestException("Token de autorización no válido o ausente");
    }
}


