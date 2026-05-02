package com.tow.backend.security;

import com.tow.backend.security.repository.JwtBlacklistRepository;
import com.tow.backend.security.entity.JwtBlacklist;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Componente que gestiona la creación y validación de tokens JWT.
 *
 * <p>Usa la librería {@code io.jsonwebtoken:jjwt} (versión 0.12.x).
 *
 * <p>Estructura del token:
 * <ul>
 *   <li>{@code sub} — email del usuario (subject)</li>
 *   <li>{@code userId} — ID numérico del usuario</li>
 *   <li>{@code roles} — lista de roles del usuario</li>
 *   <li>{@code twoFactorPending} — true si el token es temporal (2FA pendiente)</li>
 *   <li>{@code jti} — ID único del token (usado para la blacklist)</li>
 * </ul>
 *
 * @author Darío Márquez Bautista
 * @see JwtAuthenticationFilter
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expirationMs;

    private final JwtBlacklistRepository blacklistRepository;

    /** Expiración del token temporal de 2FA: 5 minutos. */
    private static final long TWO_FACTOR_EXPIRATION_MS = 5 * 60 * 1000L;

    /**
     * Genera un JWT completo para un usuario autenticado.
     *
     * @param userId  ID del usuario
     * @param email   email del usuario (subject)
     * @param roles   lista de nombres de roles (ej: ["user", "admin"])
     * @return token JWT firmado
     */
    public String generateToken(Integer userId, String email, List<String> roles) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .id(UUID.randomUUID().toString())       // jti — ID único del token
                .subject(email)                         // sub — email del usuario
                .claim("userId", userId)
                .claim("roles", roles)
                .claim("twoFactorPending", false)
                .issuedAt(now)
                .expiration(expiration)
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Genera un token temporal para el flujo de 2FA.
     *
     * <p>Este token tiene 5 minutos de vida y solo puede usarse en
     * {@code POST /api/auth/verify-2fa}. Contiene el claim
     * {@code twoFactorPending: true} para distinguirlo de un token completo.
     *
     * @param userId ID del usuario que ha superado la validación de contraseña
     * @param email  email del usuario
     * @return token JWT temporal de 2FA
     */
    public String generateTwoFactorPendingToken(Integer userId, String email) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + TWO_FACTOR_EXPIRATION_MS);

        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(email)
                .claim("userId", userId)
                .claim("twoFactorPending", true)
                .issuedAt(now)
                .expiration(expiration)
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Extrae el email (subject) de un token JWT.
     *
     * @param token token JWT
     * @return email del usuario
     */
    public String getEmailFromToken(String token) {
        return parseClaims(token).getSubject();
    }

    /**
     * Extrae el ID de usuario del token.
     *
     * @param token token JWT
     * @return ID del usuario
     */
    public Integer getUserIdFromToken(String token) {
        return parseClaims(token).get("userId", Integer.class);
    }

    /**
     * Extrae el JTI (JWT ID) del token.
     * Se usa para añadir el token a la blacklist al hacer logout.
     *
     * @param token token JWT
     * @return JTI del token
     */
    public String getJtiFromToken(String token) {
        return parseClaims(token).getId();
    }

    /**
     * Obtiene la fecha de expiración del token.
     *
     * @param token token JWT
     * @return fecha de expiración
     */
    public LocalDateTime getExpirationFromToken(String token) {
        Date expiration = parseClaims(token).getExpiration();
        return expiration.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    /**
     * Comprueba si el token tiene 2FA pendiente.
     *
     * @param token token JWT
     * @return true si es un token temporal de 2FA
     */
    public boolean isTwoFactorPending(String token) {
        Boolean pending = parseClaims(token).get("twoFactorPending", Boolean.class);
        return Boolean.TRUE.equals(pending);
    }

    /**
     * Valida un token JWT verificando firma, expiración y blacklist.
     *
     * @param token token JWT a validar
     * @return true si el token es válido y no está revocado
     */
    public boolean validateToken(String token) {
        try {
            Claims claims = parseClaims(token);

            // Comprobar si el token está en la blacklist (logout)
            String jti = claims.getId();
            if (jti != null && blacklistRepository.existsByTokenJti(jti)) {
                log.debug("Token revocado (en blacklist): {}", jti);
                return false;
            }

            return true;
        } catch (ExpiredJwtException e) {
            log.debug("Token expirado: {}", e.getMessage());
        } catch (JwtException | IllegalArgumentException e) {
            log.debug("Token inválido: {}", e.getMessage());
        }
        return false;
    }

    /**
     * Revoca un token añadiéndolo a la blacklist.
     * Se llama al hacer logout.
     *
     * @param token token JWT a revocar
     */
    public void revokeToken(String token) {
        try {
            Claims claims = parseClaims(token);
            String jti = claims.getId();
            LocalDateTime expiration = getExpirationFromToken(token);

            JwtBlacklist entry = new JwtBlacklist();
            entry.setTokenJti(jti);
            entry.setFechaExpiracion(expiration);

            blacklistRepository.save(entry);
            log.debug("Token revocado (logout): {}", jti);
        } catch (JwtException e) {
            log.warn("No se pudo revocar el token: {}", e.getMessage());
        }
    }

    // ============================================================
    // Métodos privados
    // ============================================================

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
