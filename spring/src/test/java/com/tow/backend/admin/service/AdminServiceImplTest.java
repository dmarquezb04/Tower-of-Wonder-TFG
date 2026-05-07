package com.tow.backend.admin.service;

import com.tow.backend.admin.dto.AdminMetricsDTO;
import com.tow.backend.admin.dto.UserAdminDTO;
import com.tow.backend.exception.NotFoundException;
import com.tow.backend.user.entity.Role;
import com.tow.backend.user.entity.User;
import com.tow.backend.user.repository.RoleRepository;
import com.tow.backend.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private AdminServiceImpl adminService;

    private User user1;
    private User user2;
    private Role adminRole;
    private Role userRole;

    @BeforeEach
    void setUp() {
        adminRole = new Role();
        adminRole.setNombreRol("ADMIN");

        userRole = new Role();
        userRole.setNombreRol("USER");

        user1 = User.builder()
                .idUsuario(1L)
                .email("admin@test.com")
                .username("admin")
                .activo(true)
                .twoFaEnabled(true)
                .role(adminRole)
                .fechaCreacion(LocalDateTime.now())
                .ultimoLogin(LocalDateTime.now())
                .build();

        user2 = User.builder()
                .idUsuario(2L)
                .email("user@test.com")
                .username("user")
                .activo(false)
                .twoFaEnabled(false)
                .role(userRole)
                .fechaCreacion(LocalDateTime.now())
                .ultimoLogin(LocalDateTime.now())
                .build();
    }

    @Test
    void getMetrics_ReturnsCorrectCounts() {
        when(userRepository.findAll()).thenReturn(Arrays.asList(user1, user2));

        AdminMetricsDTO metrics = adminService.getMetrics();

        assertEquals(2, metrics.getTotalUsers());
        assertEquals(1, metrics.getActiveUsers());
        assertEquals(1, metrics.getUsersWith2FA());
    }

    @Test
    void getAllUsers_ReturnsMappedDTOs() {
        when(userRepository.findAll()).thenReturn(Arrays.asList(user1, user2));

        List<UserAdminDTO> result = adminService.getAllUsers();

        assertEquals(2, result.size());
        assertEquals("admin@test.com", result.get(0).getEmail());
        assertEquals("ADMIN", result.get(0).getRole());
        assertEquals("user@test.com", result.get(1).getEmail());
        assertEquals("USER", result.get(1).getRole());
    }

    @Test
    void updateUser_Success() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(user2));
        when(roleRepository.findByNombreRol("ADMIN")).thenReturn(Optional.of(adminRole));

        adminService.updateUser(2L, "newusername", "ADMIN", true);

        assertEquals("newusername", user2.getUsername());
        assertEquals("ADMIN", user2.getRole().getNombreRol());
        assertTrue(user2.getActivo());
        verify(userRepository).save(user2);
    }

    @Test
    void updateUser_UserNotFound_ThrowsException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> 
                adminService.updateUser(99L, "newusername", "ADMIN", true));
        
        verify(userRepository, never()).save(any());
    }

    @Test
    void updateUser_RoleNotFound_ThrowsException() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(user2));
        when(roleRepository.findByNombreRol("SUPERADMIN")).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> 
                adminService.updateUser(2L, "newusername", "SUPERADMIN", true));
        
        verify(userRepository, never()).save(any());
    }

    @Test
    void deleteUser_Success() {
        when(userRepository.existsById(2L)).thenReturn(true);

        adminService.deleteUser(2L);

        verify(userRepository).deleteById(2L);
    }

    @Test
    void deleteUser_UserNotFound_ThrowsException() {
        when(userRepository.existsById(99L)).thenReturn(false);

        assertThrows(NotFoundException.class, () -> adminService.deleteUser(99L));
        
        verify(userRepository, never()).deleteById(any());
    }
}
