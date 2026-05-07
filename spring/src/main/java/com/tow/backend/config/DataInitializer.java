package com.tow.backend.config;

import com.tow.backend.user.entity.Role;
import com.tow.backend.user.entity.User;
import com.tow.backend.user.repository.RoleRepository;
import com.tow.backend.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        // 1. Inicializar roles si no existen
        if (roleRepository.count() == 0) {
            log.info("Inicializando roles del sistema...");
            roleRepository.save(new Role(null, "admin", "Administrador con acceso total"));
            roleRepository.save(new Role(null, "moderator", "Moderador con permisos especiales"));
            roleRepository.save(new Role(null, "user", "Usuario normal del sistema"));
        }

        // 2. Crear usuario administrador inicial si no hay usuarios
        if (userRepository.count() == 0) {
            log.info("Creando usuario administrador inicial...");
            Role adminRole = roleRepository.findByNombreRol("admin")
                    .orElseThrow(() -> new RuntimeException("Error: Rol admin no encontrado"));

            User admin = User.builder()
                    .email("admin@tow.com")
                    .username("admin")
                    .passwordHash(passwordEncoder.encode("admin123")) // Cambiar en producción
                    .activo(true)
                    .twoFaEnabled(false)
                    .role(adminRole)
                    .build();
            userRepository.save(admin);
            log.info("Usuario administrador creado: admin@tow.com / admin123");
        }

        // 3. Corregir roles nulos en usuarios existentes (migración)
        Role defaultRole = roleRepository.findByNombreRol("user").orElse(null);
        if (defaultRole != null) {
            List<User> usersWithoutRole = userRepository.findAll().stream()
                    .filter(u -> u.getRole() == null)
                    .collect(Collectors.toList());

            if (!usersWithoutRole.isEmpty()) {
                log.info("Asignando rol por defecto a {} usuarios...", usersWithoutRole.size());
                usersWithoutRole.forEach(u -> u.setRole(defaultRole));
                userRepository.saveAll(usersWithoutRole);
            }
        }
    }
}

