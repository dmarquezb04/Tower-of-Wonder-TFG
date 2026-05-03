package com.tow.backend.admin.service;

import com.tow.backend.admin.dto.AdminMetricsDTO;
import com.tow.backend.admin.dto.UserAdminDTO;
import com.tow.backend.user.entity.Role;
import com.tow.backend.user.entity.User;
import com.tow.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;

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

    private UserAdminDTO mapToDTO(User user) {
        List<String> roles = user.getRoles().stream()
                .map(Role::getNombreRol)
                .collect(Collectors.toList());

        return UserAdminDTO.builder()
                .idUsuario(user.getIdUsuario())
                .email(user.getEmail())
                .username(user.getUsername())
                .twoFaEnabled(user.getTwoFaEnabled())
                .activo(user.getActivo())
                .fechaCreacion(user.getFechaCreacion())
                .ultimoLogin(user.getUltimoLogin())
                .roles(roles)
                .build();
    }
}
