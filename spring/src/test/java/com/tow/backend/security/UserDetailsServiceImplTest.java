package com.tow.backend.security;

import com.tow.backend.user.entity.Role;
import com.tow.backend.user.entity.User;
import com.tow.backend.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserDetailsServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserDetailsServiceImpl userDetailsService;

    private User testUser;
    private final String TEST_EMAIL = "test@example.com";

    @BeforeEach
    void setUp() {
        Role userRole = new Role();
        userRole.setNombreRol("ADMIN");

        testUser = User.builder()
                .idUsuario(1L)
                .email(TEST_EMAIL)
                .passwordHash("hashedpassword")
                .activo(true)
                .role(userRole)
                .build();
    }

    @Test
    void loadUserByUsername_Success() {
        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(testUser));

        UserDetails userDetails = userDetailsService.loadUserByUsername(TEST_EMAIL);

        assertNotNull(userDetails);
        assertEquals(TEST_EMAIL, userDetails.getUsername());
        assertEquals("hashedpassword", userDetails.getPassword());
        
        // El role debe tener el prefijo ROLE_ (ROLE_ADMIN)
        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
    }

    @Test
    void loadUserByUsername_UserNotFound_ThrowsException() {
        when(userRepository.findByEmail("notfound@example.com")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> 
                userDetailsService.loadUserByUsername("notfound@example.com"));
    }

    @Test
    void loadUserByUsername_UserInactive_ThrowsException() {
        testUser.setActivo(false);
        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(testUser));

        assertThrows(UsernameNotFoundException.class, () -> 
                userDetailsService.loadUserByUsername(TEST_EMAIL));
    }
}
