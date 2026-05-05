package com.tow.backend.admin.service;

import com.tow.backend.admin.dto.AdminMetricsDTO;
import com.tow.backend.admin.dto.UserAdminDTO;
import com.tow.backend.user.entity.Role;
import com.tow.backend.user.entity.User;
import com.tow.backend.user.repository.UserRepository;
import com.tow.backend.user.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @Transactional(readOnly = true)
    public AdminMetricsDTO getMetrics() {
        List<User> allUsers = userRepository.findAll();
        long totalUsers = allUsers.size();
        long activeUsers = allUsers.stream().filter(u -> Boolean.TRUE.equals(u.getActivo())).count();
        long usersWith2FA = allUsers.stream().filter(u -> Boolean.TRUE.equals(u.getTwoFaEnabled())).count();

        return AdminMetricsDTO.builder()
                .totalUsers(totalUsers)
                .activeUsers(activeUsers)
                .usersWith2FA(usersWith2FA)
                .build();
    }

    @Transactional(readOnly = true)
    public List<UserAdminDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public void updateUserStatus(Long id, boolean activo) {
        log.info("Cambiando estado de usuario ID {} a activo={}", id, activo);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        user.setActivo(activo);
        userRepository.save(user);
    }

    @Transactional
    public void updateUser(Long id, String username, String roleName, boolean activo) {
        log.info("Actualizando datos de usuario ID {}: username={}, role={}, activo={}", id, username, roleName, activo);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        Role newRole = roleRepository.findByNombreRol(roleName)
                .orElseThrow(() -> new RuntimeException("Rol no encontrado: " + roleName));
        
        user.setUsername(username);
        user.setRole(newRole);
        user.setActivo(activo);
        
        userRepository.save(user);
        log.info("Usuario ID {} actualizado correctamente", id);
    }

    @Transactional
    public void deleteUser(Long id) {
        log.warn("Solicitud de borrado para usuario ID: {}", id);
        if (!userRepository.existsById(id)) {
            log.error("Fallo al borrar: El usuario con ID {} no existe", id);
            throw new RuntimeException("Usuario no encontrado");
        }
        userRepository.deleteById(id);
        log.info("Usuario ID {} eliminado con éxito", id);
    }

    private UserAdminDTO mapToDTO(User user) {
        return UserAdminDTO.builder()
                .idUsuario(user.getIdUsuario())
                .email(user.getEmail())
                .username(user.getUsername())
                .twoFaEnabled(user.getTwoFaEnabled())
                .activo(user.getActivo())
                .fechaCreacion(user.getFechaCreacion())
                .ultimoLogin(user.getUltimoLogin())
                .role(user.getRole().getNombreRol())
                .build();
    }
}
