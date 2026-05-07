package com.tow.backend.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filtro de Spring Security que intercepta cada peticiÃ³n HTTP y,
 * si incluye un JWT vÃ¡lido en el header {@code Authorization},
 * autentica al usuario en el contexto de seguridad.
 *
 * <p>Hereda de {@link OncePerRequestFilter} para garantizar que
 * se ejecuta exactamente una vez por peticiÃ³n.
 *
 * <p>Flujo de cada peticiÃ³n:
 * <ol>
 *   <li>Extraer el token del header {@code Authorization: Bearer <token>}</li>
 *   <li>Validar el token (firma, expiraciÃ³n, blacklist)</li>
 *   <li>Cargar el usuario de la BD</li>
 *   <li>Establecer la autenticaciÃ³n en {@link SecurityContextHolder}</li>
 *   <li>Continuar con la cadena de filtros</li>
 * </ol>
 *
 * @author DarÃ­o MÃ¡rquez Bautista
 * @see JwtTokenProvider
 * @see com.tow.backend.config.SecurityConfig
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider tokenProvider;
    private final UserDetailsServiceImpl userDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        String token = extractTokenFromRequest(request);

        if (StringUtils.hasText(token) && tokenProvider.validateToken(token)) {
            // El token es vÃ¡lido â†’ autenticar al usuario
            String email = tokenProvider.getEmailFromToken(token);

            // Tokens de 2FA pendiente NO autentican â€” solo sirven para /verify-2fa
            if (!tokenProvider.isTwoFactorPending(token)) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(email);

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );
                authentication.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );

                SecurityContextHolder.getContext().setAuthentication(authentication);
                log.debug("Usuario autenticado vÃ­a JWT: {}", email);
            }
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Extrae el token JWT del header {@code Authorization}.
     *
     * <p>El formato esperado es: {@code Authorization: Bearer eyJhbGciOiJI...}
     *
     * @param request peticiÃ³n HTTP
     * @return el token sin el prefijo "Bearer ", o null si no estÃ¡ presente
     */
    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}

