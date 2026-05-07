package com.tow.backend.user.service;

import com.tow.backend.email.service.MailService;
import com.tow.backend.exception.BadRequestException;
import com.tow.backend.exception.ConflictException;
import com.tow.backend.exception.NotFoundException;
import com.tow.backend.user.dto.TwoFactorSetupDTO;
import com.tow.backend.user.dto.UpdateProfileRequest;
import com.tow.backend.user.dto.UserProfileDTO;
import com.tow.backend.user.entity.Role;
import com.tow.backend.user.entity.User;
import com.tow.backend.user.repository.UserRepository;
import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private GoogleAuthenticator googleAuthenticator;

    @Mock
    private MailService mailService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;
    private final String TEST_EMAIL = "test@example.com";

    @BeforeEach
    void setUp() {
        Role userRole = new Role();
        userRole.setNombreRol("ROLE_USER");

        testUser = User.builder()
                .idUsuario(1L)
                .email(TEST_EMAIL)
                .username("testuser")
                .passwordHash("hashedpassword")
                .activo(true)
                .twoFaEnabled(false)
                .role(userRole)
                .build();
        
        // Inject frontendUrl since it's an @Value field
        ReflectionTestUtils.setField(userService, "frontendUrl", "http://localhost:3000");
    }

    @Test
    void getUserProfile_Success() {
        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(testUser));

        UserProfileDTO profile = userService.getUserProfile(TEST_EMAIL);

        assertNotNull(profile);
        assertEquals(TEST_EMAIL, profile.getEmail());
        assertEquals("testuser", profile.getUsername());
        assertEquals("ROLE_USER", profile.getRole());
    }

    @Test
    void getUserProfile_UserNotFound_ThrowsException() {
        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> userService.getUserProfile(TEST_EMAIL));
    }

    @Test
    void generateTwoFactorSetup_Success() {
        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(testUser));
        
        GoogleAuthenticatorKey mockKey = mock(GoogleAuthenticatorKey.class);
        when(mockKey.getKey()).thenReturn("SECRET_KEY");
        when(googleAuthenticator.createCredentials()).thenReturn(mockKey);

        TwoFactorSetupDTO setupDTO = userService.generateTwoFactorSetup(TEST_EMAIL);

        assertNotNull(setupDTO);
        assertEquals("SECRET_KEY", setupDTO.getSecret());
        assertTrue(setupDTO.getQrCodeUri().contains("SECRET_KEY"));
        assertTrue(setupDTO.getQrCodeUri().contains(testUser.getUsername()));
    }

    @Test
    void generateTwoFactorSetup_AlreadyEnabled_ThrowsException() {
        testUser.setTwoFaEnabled(true);
        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(testUser));

        assertThrows(ConflictException.class, () -> userService.generateTwoFactorSetup(TEST_EMAIL));
    }

    @Test
    void enableTwoFactor_Success() {
        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(testUser));
        when(googleAuthenticator.authorize("SECRET_KEY", 123456)).thenReturn(true);

        userService.enableTwoFactor(TEST_EMAIL, "SECRET_KEY", "123456");

        assertTrue(testUser.getTwoFaEnabled());
        assertEquals("SECRET_KEY", testUser.getTwofaSecret());
        verify(userRepository).save(testUser);
    }

    @Test
    void enableTwoFactor_InvalidCode_ThrowsException() {
        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(testUser));
        when(googleAuthenticator.authorize("SECRET_KEY", 123456)).thenReturn(false);

        assertThrows(BadRequestException.class, () -> userService.enableTwoFactor(TEST_EMAIL, "SECRET_KEY", "123456"));
        verify(userRepository, never()).save(any());
    }

    @Test
    void disableTwoFactor_Success() {
        testUser.setTwoFaEnabled(true);
        testUser.setTwofaSecret("SECRET_KEY");
        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(testUser));
        when(googleAuthenticator.authorize("SECRET_KEY", 123456)).thenReturn(true);

        userService.disableTwoFactor(TEST_EMAIL, "123456");

        assertFalse(testUser.getTwoFaEnabled());
        assertNull(testUser.getTwofaSecret());
        verify(userRepository).save(testUser);
    }

    @Test
    void disableTwoFactor_NotEnabled_ThrowsException() {
        testUser.setTwoFaEnabled(false);
        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(testUser));

        assertThrows(BadRequestException.class, () -> userService.disableTwoFactor(TEST_EMAIL, "123456"));
    }

    @Test
    void deleteAccount_Success() {
        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(testUser));

        userService.deleteAccount(TEST_EMAIL);

        assertFalse(testUser.getActivo());
        assertNotNull(testUser.getRecoveryToken());
        assertNotNull(testUser.getRecoveryTokenExpiry());
        verify(userRepository).save(testUser);
        
        ArgumentCaptor<Map<String, Object>> mapCaptor = ArgumentCaptor.forClass(Map.class);
        verify(mailService).sendHtmlEmail(eq(TEST_EMAIL), anyString(), eq("account_deactivation"), mapCaptor.capture());
        assertTrue(mapCaptor.getValue().containsKey("reactivationLink"));
    }

    @Test
    void reactivateAccount_Success() {
        testUser.setActivo(false);
        testUser.setRecoveryToken("TOKEN");
        testUser.setRecoveryTokenExpiry(LocalDateTime.now().plusDays(1));
        when(userRepository.findByRecoveryToken("TOKEN")).thenReturn(Optional.of(testUser));

        userService.reactivateAccount("TOKEN");

        assertTrue(testUser.getActivo());
        assertNull(testUser.getRecoveryToken());
        assertNull(testUser.getRecoveryTokenExpiry());
        verify(userRepository).save(testUser);
    }

    @Test
    void reactivateAccount_ExpiredToken_ThrowsException() {
        testUser.setActivo(false);
        testUser.setRecoveryToken("TOKEN");
        testUser.setRecoveryTokenExpiry(LocalDateTime.now().minusDays(1));
        when(userRepository.findByRecoveryToken("TOKEN")).thenReturn(Optional.of(testUser));

        assertThrows(BadRequestException.class, () -> userService.reactivateAccount("TOKEN"));
    }

    @Test
    void updateProfile_UsernameAndPass_Success() {
        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("oldpass", "hashedpassword")).thenReturn(true);
        when(passwordEncoder.encode("newpass")).thenReturn("newhashedpassword");

        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setUsername("newusername");
        request.setCurrentPassword("oldpass");
        request.setNewPassword("newpass");

        userService.updateProfile(TEST_EMAIL, request);

        assertEquals("newusername", testUser.getUsername());
        assertEquals("newhashedpassword", testUser.getPasswordHash());
        verify(userRepository).save(testUser);
        verify(mailService).sendHtmlEmail(eq(TEST_EMAIL), anyString(), eq("credential_update"), any());
    }

    @Test
    void updateProfile_MissingCurrentPass_ThrowsException() {
        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(testUser));

        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setNewPassword("newpass");

        assertThrows(BadRequestException.class, () -> userService.updateProfile(TEST_EMAIL, request));
        verify(userRepository, never()).save(any());
    }

    @Test
    void updateProfile_WrongCurrentPass_ThrowsException() {
        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("wrongpass", "hashedpassword")).thenReturn(false);

        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setCurrentPassword("wrongpass");
        request.setNewPassword("newpass");

        assertThrows(BadRequestException.class, () -> userService.updateProfile(TEST_EMAIL, request));
        verify(userRepository, never()).save(any());
    }
}
