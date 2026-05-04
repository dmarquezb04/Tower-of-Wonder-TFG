package com.tow.backend.security;

import com.tow.backend.security.repository.JwtBlacklistRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para {@link JwtTokenProvider}.
 *
 * <p>Se usa Mockito para simular el repositorio de la blacklist,
 * evitando necesitar una base de datos real.
 *
 * <p>Se usa AssertJ ({@code assertThat}) en lugar de los asserts de JUnit
 * porque produce mensajes de error más descriptivos.
 *
 * @author Darío Márquez Bautista
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("JwtTokenProvider — Tests")
class JwtTokenProviderTest {

    @Mock
    private JwtBlacklistRepository blacklistRepository;

    @InjectMocks
    private JwtTokenProvider tokenProvider;

    /** Secreto de al menos 32 chars (requerido por HS384) */
    private static final String TEST_SECRET =
            "test-secret-key-tower-of-wonder-min-32-chars-ok";

    @BeforeEach
    void setUp() {
        // Inyectar valores de @Value manualmente (sin levantar el contexto Spring)
        ReflectionTestUtils.setField(tokenProvider, "secret", TEST_SECRET);
        ReflectionTestUtils.setField(tokenProvider, "expirationMs", 3600000L); // 1h
    }

    // ================================================================
    // generateToken
    // ================================================================
    @Nested
    @DisplayName("generateToken()")
    class GenerateToken {

        @Test
        @DisplayName("Genera un token no vacío")
        void generateToken_returnsNonBlankToken() {
            String token = tokenProvider.generateToken(1L, "user@test.com", List.of("user"));
            assertThat(token).isNotBlank();
        }

        @Test
        @DisplayName("El token contiene tres partes (header.payload.signature)")
        void generateToken_hasThreeParts() {
            String token = tokenProvider.generateToken(1L, "user@test.com", List.of("user"));
            assertThat(token.split("\\.")).hasSize(3);
        }
    }

    // ================================================================
    // getEmailFromToken
    // ================================================================
    @Nested
    @DisplayName("getEmailFromToken()")
    class GetEmail {

        @Test
        @DisplayName("Extrae el email correcto del token")
        void getEmailFromToken_returnsCorrectEmail() {
            String email = "usuario@tow.com";
            String token = tokenProvider.generateToken(42L, email, List.of("user"));

            assertThat(tokenProvider.getEmailFromToken(token)).isEqualTo(email);
        }
    }

    // ================================================================
    // getUserIdFromToken
    // ================================================================
    @Nested
    @DisplayName("getUserIdFromToken()")
    class GetUserId {

        @Test
        @DisplayName("Extrae el userId correcto del token")
        void getUserIdFromToken_returnsCorrectId() {
            String token = tokenProvider.generateToken(99L, "test@tow.com", List.of("admin"));

            assertThat(tokenProvider.getUserIdFromToken(token)).isEqualTo(99L);
        }
    }

    // ================================================================
    // validateToken
    // ================================================================
    @Nested
    @DisplayName("validateToken()")
    class ValidateToken {

        @Test
        @DisplayName("Un token válido recién generado se valida correctamente")
        void validateToken_validToken_returnsTrue() {
            String token = tokenProvider.generateToken(1L, "user@test.com", List.of("user"));
            when(blacklistRepository.existsByTokenJti(any())).thenReturn(false);

            assertThat(tokenProvider.validateToken(token)).isTrue();
        }

        @Test
        @DisplayName("Un token con firma incorrecta no se valida")
        void validateToken_tamperedToken_returnsFalse() {
            String token = tokenProvider.generateToken(1L, "user@test.com", List.of("user"));
            // Modificar la firma: reemplazar los últimos 5 chars
            String tampered = token.substring(0, token.length() - 5) + "XXXXX";

            assertThat(tokenProvider.validateToken(tampered)).isFalse();
        }

        @Test
        @DisplayName("Un token revocado (en blacklist) no se valida")
        void validateToken_blacklistedToken_returnsFalse() {
            String token = tokenProvider.generateToken(1L, "user@test.com", List.of("user"));
            when(blacklistRepository.existsByTokenJti(any())).thenReturn(true);

            assertThat(tokenProvider.validateToken(token)).isFalse();
        }

        @Test
        @DisplayName("Un string vacío no se valida")
        void validateToken_emptyString_returnsFalse() {
            assertThat(tokenProvider.validateToken("")).isFalse();
        }

        @Test
        @DisplayName("Un string que no es JWT no se valida")
        void validateToken_invalidString_returnsFalse() {
            assertThat(tokenProvider.validateToken("esto-no-es-un-jwt")).isFalse();
        }
    }

    // ================================================================
    // isTwoFactorPending
    // ================================================================
    @Nested
    @DisplayName("isTwoFactorPending()")
    class TwoFactorPending {

        @Test
        @DisplayName("Un token normal tiene twoFactorPending = false")
        void isTwoFactorPending_normalToken_returnsFalse() {
            String token = tokenProvider.generateToken(1L, "user@test.com", List.of("user"));
            assertThat(tokenProvider.isTwoFactorPending(token)).isFalse();
        }

        @Test
        @DisplayName("Un token temporal de 2FA tiene twoFactorPending = true")
        void isTwoFactorPending_pendingToken_returnsTrue() {
            String token = tokenProvider.generateTwoFactorPendingToken(1L, "user@test.com");
            assertThat(tokenProvider.isTwoFactorPending(token)).isTrue();
        }
    }

    // ================================================================
    // revokeToken
    // ================================================================
    @Nested
    @DisplayName("revokeToken()")
    class RevokeToken {

        @Test
        @DisplayName("Revocar un token guarda el JTI en la blacklist")
        void revokeToken_savesJtiToBlacklist() {
            String token = tokenProvider.generateToken(1L, "user@test.com", List.of("user"));

            tokenProvider.revokeToken(token);

            // Verificar que se llamó al repositorio para guardar el JTI
            verify(blacklistRepository, times(1)).save(any());
        }

        @Test
        @DisplayName("Revocar un token inválido no lanza excepción")
        void revokeToken_invalidToken_doesNotThrow() {
            assertThatNoException()
                    .isThrownBy(() -> tokenProvider.revokeToken("token-invalido"));
        }
    }
}
