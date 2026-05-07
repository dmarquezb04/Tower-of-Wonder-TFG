package com.tow.backend.auth.service;

import com.tow.backend.auth.dto.LoginRequest;
import com.tow.backend.auth.dto.LoginResponse;
import com.tow.backend.auth.dto.RegisterRequest;
import com.tow.backend.auth.dto.TwoFactorRequest;
import com.tow.backend.exception.BadRequestException;
import com.tow.backend.exception.ConflictException;
import com.tow.backend.exception.UnauthorizedException;

/**
 * Contrato del servicio de autenticaciÃ³n.
 *
 * <p>Define las operaciones de login, verificaciÃ³n de 2FA, registro y logout.
 *
 * @author DarÃ­o MÃ¡rquez Bautista
 */
public interface AuthService {

    /**
     * Procesa el intento de login con email y contraseÃ±a.
     *
     * <p>Si el usuario tiene 2FA activo, devuelve un token temporal y
     * {@code requiresTwoFactor = true}; el cliente debe llamar a {@link #verifyTwoFactor}.
     * Si no tiene 2FA, devuelve directamente el token completo.
     *
     * @param request DTO con email y contraseÃ±a
     * @return respuesta con el token JWT y el estado del 2FA
     * @throws org.springframework.security.authentication.BadCredentialsException si las credenciales son incorrectas o la cuenta estÃ¡ desactivada
     */
    LoginResponse login(LoginRequest request);

    /**
     * Verifica el cÃ³digo TOTP y completa el login cuando el usuario tiene 2FA activo.
     *
     * <p>Requiere el token temporal generado en {@link #login} en el header Authorization.
     *
     * @param tokenTemporal token temporal de 2FA (obtenido en login)
     * @param request       DTO con el cÃ³digo TOTP de 6 dÃ­gitos
     * @return token completo si el cÃ³digo es vÃ¡lido
     * @throws UnauthorizedException si el token temporal es invÃ¡lido, expirado o el cÃ³digo es incorrecto
     * @throws BadRequestException   si el cÃ³digo TOTP no tiene el formato correcto
     */
    LoginResponse verifyTwoFactor(String tokenTemporal, TwoFactorRequest request);

    /**
     * Registra un nuevo usuario en el sistema.
     *
     * @param request DTO con email, username y contraseÃ±a
     * @throws ConflictException si el email o el nombre de usuario ya estÃ¡n en uso
     */
    void register(RegisterRequest request);

    /**
     * Revoca el token JWT del usuario, efectuando un logout real.
     *
     * <p>El token queda en la blacklist y no puede ser reutilizado aunque no haya expirado.
     *
     * @param token token JWT a revocar (sin el prefijo "Bearer ")
     */
    void logout(String token);
}


