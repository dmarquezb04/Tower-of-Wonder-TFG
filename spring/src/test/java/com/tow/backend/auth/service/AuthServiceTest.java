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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.tow.backend.exception.ConflictException;
import com.tow.backend.exception.UnauthorizedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para {@link AuthService}.
 *
 * <p>
 * Estrategia: cada dependencia se sustituye por un mock de Mockito.
 * Esto permite testear solo la lÃ³gica de AuthService de forma aislada,
 * sin base de datos ni Spring Boot corriendo (tests rÃ¡pidos).
 *
 * <p>
 * Estructura: tests agrupados por mÃ©todo con {@code @Nested},
 * lo que genera un informe mÃ¡s legible en el IDE.
 *
 * @author DarÃ­o MÃ¡rquez Bautista
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService â€” Tests")
class AuthServiceTest {

    // Mocks â€” simulan las dependencias
    @Mock
    private UserRepository userRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private JwtTokenProvider tokenProvider;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private GoogleAuthenticator googleAuthenticator;

    // El objeto real que estamos testeando
    @InjectMocks
    private AuthServiceImpl authService;

    // Datos de prueba reutilizables
    private User activeUser;
    private User twoFaUser;
    private Role userRole;

    @BeforeEach
    void setUp() {
        userRole = new Role();
        userRole.setIdRol(3L);
        userRole.setNombreRol("user");

        // Usuario normal sin 2FA
        activeUser = new User();
        activeUser.setIdUsuario(1L);
        activeUser.setEmail("user@tow.com");
        activeUser.setUsername("testuser");
        activeUser.setPasswordHash("$2a$10$hashedpassword");
        activeUser.setActivo(true);
        activeUser.setTwoFaEnabled(false);
        activeUser.setRole(userRole);

        // Usuario con 2FA activo
        twoFaUser = new User();
        twoFaUser.setIdUsuario(2L);
        twoFaUser.setEmail("admin@tow.com");
        twoFaUser.setUsername("adminuser");
        twoFaUser.setPasswordHash("$2a$10$hashedpassword2fa");
        twoFaUser.setActivo(true);
        twoFaUser.setTwoFaEnabled(true);
        twoFaUser.setTwofaSecret("JBSWY3DPEHPK3PXP");
        twoFaUser.setRole(userRole);
    }

    // ================================================================
    // login()
    // ================================================================
    @Nested
    @DisplayName("login()")
    class Login {

        @Test
        @DisplayName("Login correcto sin 2FA devuelve token completo")
        void login_validCredentials_noTwoFa_returnsFullToken() {
            // GIVEN
            LoginRequest request = new LoginRequest();
            request.setEmail("user@tow.com");
            request.setPassword("Password123!");

            when(userRepository.findByEmail("user@tow.com")).thenReturn(Optional.of(activeUser));
            when(passwordEncoder.matches("Password123!", activeUser.getPasswordHash())).thenReturn(true);
            when(tokenProvider.generateToken(anyLong(), anyString(), anyList())).thenReturn("jwt-token-full");

            // WHEN
            LoginResponse response = authService.login(request);

            // THEN
            assertThat(response.isRequiresTwoFactor()).isFalse();
            assertThat(response.getToken()).isEqualTo("jwt-token-full");
            assertThat(response.getEmail()).isEqualTo("user@tow.com");
            // No debe llamar al mÃ©todo de token temporal
            verify(tokenProvider, never()).generateTwoFactorPendingToken(anyLong(), anyString());
        }

        @Test
        @DisplayName("Login correcto con 2FA devuelve token temporal y requiresTwoFactor=true")
        void login_validCredentials_withTwoFa_returnsTempToken() {
            // GIVEN
            LoginRequest request = new LoginRequest();
            request.setEmail("admin@tow.com");
            request.setPassword("Password123!");

            when(userRepository.findByEmail("admin@tow.com")).thenReturn(Optional.of(twoFaUser));
            when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
            when(tokenProvider.generateTwoFactorPendingToken(anyLong(), anyString()))
                    .thenReturn("jwt-token-temp");

            // WHEN
            LoginResponse response = authService.login(request);

            // THEN
            assertThat(response.isRequiresTwoFactor()).isTrue();
            assertThat(response.getToken()).isEqualTo("jwt-token-temp");
            // No debe generar token completo
            verify(tokenProvider, never()).generateToken(anyLong(), anyString(), anyList());
        }

        @Test
        @DisplayName("Login con email inexistente lanza BadCredentialsException")
        void login_unknownEmail_throwsBadCredentials() {
            // GIVEN
            LoginRequest request = new LoginRequest();
            request.setEmail("noexiste@tow.com");
            request.setPassword("Password123!");

            when(userRepository.findByEmail("noexiste@tow.com")).thenReturn(Optional.empty());

            // WHEN + THEN
            assertThatThrownBy(() -> authService.login(request))
                    .isInstanceOf(BadCredentialsException.class)
                    .hasMessage("Credenciales incorrectas");
        }

        @Test
        @DisplayName("Login con contraseÃ±a incorrecta lanza BadCredentialsException")
        void login_wrongPassword_throwsBadCredentials() {
            // GIVEN
            LoginRequest request = new LoginRequest();
            request.setEmail("user@tow.com");
            request.setPassword("WrongPass!");

            when(userRepository.findByEmail("user@tow.com")).thenReturn(Optional.of(activeUser));
            when(passwordEncoder.matches("WrongPass!", activeUser.getPasswordHash())).thenReturn(false);

            // WHEN + THEN
            assertThatThrownBy(() -> authService.login(request))
                    .isInstanceOf(BadCredentialsException.class)
                    .hasMessage("Credenciales incorrectas");
        }

        @Test
        @DisplayName("Login con cuenta desactivada lanza BadCredentialsException")
        void login_inactiveAccount_throwsBadCredentials() {
            // GIVEN
            activeUser.setActivo(false);

            LoginRequest request = new LoginRequest();
            request.setEmail("user@tow.com");
            request.setPassword("Password123!");

            when(userRepository.findByEmail("user@tow.com")).thenReturn(Optional.of(activeUser));
            when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);

            // WHEN + THEN
            assertThatThrownBy(() -> authService.login(request))
                    .isInstanceOf(BadCredentialsException.class)
                    .hasMessage("La cuenta estÃ¡ desactivada");
        }

        @Test
        @DisplayName("Login exitoso actualiza el campo ultimoLogin del usuario")
        void login_success_updatesLastLogin() {
            // GIVEN
            LoginRequest request = new LoginRequest();
            request.setEmail("user@tow.com");
            request.setPassword("Password123!");

            when(userRepository.findByEmail("user@tow.com")).thenReturn(Optional.of(activeUser));
            when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
            when(tokenProvider.generateToken(anyLong(), anyString(), anyList())).thenReturn("token");

            // WHEN
            authService.login(request);

            // THEN â€” se debe haber guardado el usuario (para actualizar ultimoLogin)
            verify(userRepository).save(any(User.class));
        }
    }

    // ================================================================
    // register()
    // ================================================================
    @Nested
    @DisplayName("register()")
    class Register {

        @Test
        @DisplayName("Registro con datos vÃ¡lidos crea el usuario correctamente")
        void register_validData_savesUser() {
            // GIVEN
            RegisterRequest request = new RegisterRequest();
            request.setEmail("nuevo@tow.com");
            request.setUsername("nuevousuario");
            request.setPassword("Password123!");

            when(userRepository.existsByEmail("nuevo@tow.com")).thenReturn(false);
            when(userRepository.existsByUsername("nuevousuario")).thenReturn(false);
            when(roleRepository.findByNombreRol("user")).thenReturn(Optional.of(userRole));
            when(passwordEncoder.encode("Password123!")).thenReturn("$2a$10$hashedNewPass");

            // WHEN
            authService.register(request);

            // THEN â€” se debe haber guardado un usuario nuevo
            verify(userRepository).save(argThat(user -> user.getEmail().equals("nuevo@tow.com") &&
                    user.getUsername().equals("nuevousuario") &&
                    user.getPasswordHash().equals("$2a$10$hashedNewPass") &&
                    !user.getTwoFaEnabled() &&
                    user.getRole().equals(userRole)));
        }

        @Test
        @DisplayName("Registro con email duplicado lanza ConflictException")
        void register_duplicateEmail_throwsException() {
            // GIVEN
            RegisterRequest request = new RegisterRequest();
            request.setEmail("user@tow.com");
            request.setUsername("otro");
            request.setPassword("Password123!");

            when(userRepository.existsByEmail("user@tow.com")).thenReturn(true);

            // WHEN + THEN
            assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(ConflictException.class)
                .hasMessage("El email ya estÃ¡ registrado");

            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("Registro con username duplicado lanza ConflictException")
        void register_duplicateUsername_throwsException() {
            // GIVEN
            RegisterRequest request = new RegisterRequest();
            request.setEmail("nuevo@tow.com");
            request.setUsername("testuser"); // ya existe
            request.setPassword("Password123!");

            when(userRepository.existsByEmail("nuevo@tow.com")).thenReturn(false);
            when(userRepository.existsByUsername("testuser")).thenReturn(true);

            // WHEN + THEN
            assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(ConflictException.class)
                .hasMessage("El nombre de usuario ya estÃ¡ en uso");

            verify(userRepository, never()).save(any());
        }
    }

    // ================================================================
    // verifyTwoFactor()
    // ================================================================
    @Nested
    @DisplayName("verifyTwoFactor()")
    class VerifyTwoFactor {

        @Test
        @DisplayName("VerificaciÃ³n 2FA correcta devuelve token completo")
        void verifyTwoFactor_validCode_returnsFullToken() {
            // GIVEN
            String tempToken = "temp-jwt-token";
            TwoFactorRequest request = new TwoFactorRequest();
            request.setCode("123456");

            when(tokenProvider.validateToken(tempToken)).thenReturn(true);
            when(tokenProvider.isTwoFactorPending(tempToken)).thenReturn(true);
            when(tokenProvider.getEmailFromToken(tempToken)).thenReturn("admin@tow.com");
            when(userRepository.findByEmail("admin@tow.com")).thenReturn(Optional.of(twoFaUser));
            when(googleAuthenticator.authorize("JBSWY3DPEHPK3PXP", 123456)).thenReturn(true);
            when(tokenProvider.generateToken(anyLong(), anyString(), anyList())).thenReturn("full-jwt");

            // WHEN
            LoginResponse response = authService.verifyTwoFactor(tempToken, request);

            // THEN
            assertThat(response.isRequiresTwoFactor()).isFalse();
            assertThat(response.getToken()).isEqualTo("full-jwt");
            // El token temporal se revoca
            verify(tokenProvider).revokeToken(tempToken);
        }

        @Test
        @DisplayName("VerificaciÃ³n 2FA con cÃ³digo incorrecto lanza UnauthorizedException")
        void verifyTwoFactor_wrongCode_throwsBadCredentials() {
            // GIVEN
            String tempToken = "temp-jwt-token";
            TwoFactorRequest request = new TwoFactorRequest();
            request.setCode("000000");

            when(tokenProvider.validateToken(tempToken)).thenReturn(true);
            when(tokenProvider.isTwoFactorPending(tempToken)).thenReturn(true);
            when(tokenProvider.getEmailFromToken(tempToken)).thenReturn("admin@tow.com");
            when(userRepository.findByEmail("admin@tow.com")).thenReturn(Optional.of(twoFaUser));
            when(googleAuthenticator.authorize("JBSWY3DPEHPK3PXP", 0)).thenReturn(false);

            // WHEN + THEN
            assertThatThrownBy(() -> authService.verifyTwoFactor(tempToken, request))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("CÃ³digo de verificaciÃ³n incorrecto");
        }

        @Test
        @DisplayName("VerificaciÃ³n 2FA con token invÃ¡lido lanza UnauthorizedException")
        void verifyTwoFactor_invalidToken_throwsBadCredentials() {
            // GIVEN
            TwoFactorRequest request = new TwoFactorRequest();
            request.setCode("123456");

            when(tokenProvider.validateToken("bad-token")).thenReturn(false);

            // WHEN + THEN
            assertThatThrownBy(() -> authService.verifyTwoFactor("bad-token", request))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("Token de verificaciÃ³n invÃ¡lido o expirado");
        }
    }

    // ================================================================
    // logout()
    // ================================================================
    @Nested
    @DisplayName("logout()")
    class Logout {

        @Test
        @DisplayName("Logout revoca el token correctamente")
        void logout_validToken_revokesIt() {
            // WHEN
            authService.logout("any-jwt-token");

            // THEN
            verify(tokenProvider).revokeToken("any-jwt-token");
        }
    }
}

