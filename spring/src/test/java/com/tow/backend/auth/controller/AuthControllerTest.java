package com.tow.backend.auth.controller;

import com.tow.backend.auth.dto.LoginRequest;
import com.tow.backend.auth.dto.LoginResponse;
import com.tow.backend.auth.dto.RegisterRequest;
import com.tow.backend.auth.dto.TwoFactorRequest;
import com.tow.backend.auth.service.AuthService;
import com.tow.backend.common.dto.ApiResponse;
import com.tow.backend.exception.BadRequestException;
import com.tow.backend.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;

    @Mock
    private UserService userService;

    @InjectMocks
    private AuthController authController;

    private LoginRequest loginRequest;
    private LoginResponse loginResponse;

    @BeforeEach
    void setUp() {
        loginRequest = new LoginRequest();
        loginRequest.setEmail("test@test.com");
        loginRequest.setPassword("password123");

        loginResponse = LoginResponse.builder()
                .token("jwt.token.here")
                .requiresTwoFactor(false)
                .build();
    }

    @Test
    void login_Success() {
        when(authService.login(loginRequest)).thenReturn(loginResponse);

        ResponseEntity<LoginResponse> response = authController.login(loginRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(loginResponse, response.getBody());
    }

    @Test
    void verifyTwoFactor_Success() {
        TwoFactorRequest request = new TwoFactorRequest();
        request.setCode("123456");

        when(authService.verifyTwoFactor("temp.token", request)).thenReturn(loginResponse);

        ResponseEntity<LoginResponse> response = authController.verifyTwoFactor("Bearer temp.token", request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(loginResponse, response.getBody());
    }

    @Test
    void verifyTwoFactor_MissingBearer_ThrowsException() {
        TwoFactorRequest request = new TwoFactorRequest();
        request.setCode("123456");

        assertThrows(BadRequestException.class, () -> 
            authController.verifyTwoFactor("temp.token", request)
        );
    }

    @Test
    void register_Success() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("new@test.com");
        request.setUsername("newuser");
        request.setPassword("password123");

        doNothing().when(authService).register(request);

        ResponseEntity<ApiResponse> response = authController.register(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("Usuario registrado correctamente", response.getBody().getMessage());
    }

    @Test
    void logout_Success() {
        doNothing().when(authService).logout("jwt.token.here");

        ResponseEntity<ApiResponse> response = authController.logout("Bearer jwt.token.here");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Sesión cerrada correctamente", response.getBody().getMessage());
    }

    @Test
    void logout_InvalidHeader_ThrowsException() {
        assertThrows(BadRequestException.class, () -> 
            authController.logout("InvalidHeaderFormat")
        );
    }

    @Test
    void reactivateAccount_Success() {
        doNothing().when(userService).reactivateAccount("valid-token");

        ResponseEntity<ApiResponse> response = authController.reactivateAccount("valid-token");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Cuenta reactivada correctamente. Ya puedes iniciar sesión.", response.getBody().getMessage());
    }

    @Test
    void reactivateAccount_EmptyToken_ThrowsException() {
        assertThrows(BadRequestException.class, () -> 
            authController.reactivateAccount("")
        );
    }
}
