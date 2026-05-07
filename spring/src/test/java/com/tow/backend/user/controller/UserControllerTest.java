package com.tow.backend.user.controller;

import com.tow.backend.user.dto.TwoFactorCodeDTO;
import com.tow.backend.user.dto.TwoFactorSetupDTO;
import com.tow.backend.user.dto.UserProfileDTO;
import com.tow.backend.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        userDetails = new User("test@test.com", "password", Collections.emptyList());
    }

    @Test
    void getProfile_ReturnsUserProfile() {
        UserProfileDTO expectedProfile = new UserProfileDTO();
        when(userService.getUserProfile("test@test.com")).thenReturn(expectedProfile);

        ResponseEntity<UserProfileDTO> response = userController.getProfile(userDetails);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(expectedProfile, response.getBody());
    }

    @Test
    void setupTwoFactor_ReturnsSetupDTO() {
        TwoFactorSetupDTO expectedSetup = new TwoFactorSetupDTO();
        when(userService.generateTwoFactorSetup("test@test.com")).thenReturn(expectedSetup);

        ResponseEntity<TwoFactorSetupDTO> response = userController.setupTwoFactor(userDetails);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(expectedSetup, response.getBody());
    }

    @Test
    void enableTwoFactor_ValidCode_ReturnsOk() {
        TwoFactorCodeDTO request = new TwoFactorCodeDTO();
        request.setSecret("JBSWY3DPEHPK3PXP");
        request.setCode("123456");

        doNothing().when(userService).enableTwoFactor("test@test.com", "JBSWY3DPEHPK3PXP", "123456");

        ResponseEntity<?> response = userController.enableTwoFactor(userDetails, request);

        assertEquals(200, response.getStatusCode().value());
    }
}


