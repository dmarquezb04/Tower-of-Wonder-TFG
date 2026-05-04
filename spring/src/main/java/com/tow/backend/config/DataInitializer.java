package com.tow.backend.config;

import com.tow.backend.user.entity.Role;
import com.tow.backend.user.entity.User;
import com.tow.backend.user.repository.RoleRepository;
import com.tow.backend.user.repository.UserRepository;
import com.tow.backend.shop.entity.Product;
import com.tow.backend.shop.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final ProductRepository productRepository;
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

        // 4. Inicializar productos si la tienda está vacía
        if (productRepository.count() == 0) {
            log.info("Inicializando datos de prueba para la Tienda...");

            List<Product> products = List.of(
                Product.builder()
                    .name("Bendición Lunar")
                    .description("Suscripción de 30 días")
                    .price(new BigDecimal("4.99"))
                    .stock(999)
                    .category("Bendición Lunar")
                    .active(true)
                    .build(),
                Product.builder()
                    .name("PB del Gnóstico")
                    .description("Desbloquea el Pase de Batalla premium")
                    .price(new BigDecimal("9.99"))
                    .stock(999)
                    .category("Pase de Batalla")
                    .active(true)
                    .build(),
                Product.builder()
                    .name("Notas del Viajero")
                    .description("Niveles adicionales del PB")
                    .price(new BigDecimal("11.99"))
                    .stock(999)
                    .category("Pase de Batalla")
                    .active(true)
                    .build(),
                Product.builder()
                    .name("PB Himno de la Perla")
                    .description("El mejor pase de batalla")
                    .price(new BigDecimal("19.99"))
                    .stock(999)
                    .category("Pase de Batalla")
                    .active(true)
                    .build(),
                Product.builder()
                    .name("Cristal génesis ×60")
                    .description("¡Doble! +60")
                    .price(new BigDecimal("0.99"))
                    .stock(999)
                    .category("Cristal génesis")
                    .active(true)
                    .build(),
                Product.builder()
                    .name("Cristal génesis ×300")
                    .description("¡Doble! +300")
                    .price(new BigDecimal("4.99"))
                    .stock(999)
                    .category("Cristal génesis")
                    .active(true)
                    .build(),
                Product.builder()
                    .name("Cristal génesis ×980")
                    .description("¡Doble! +980")
                    .price(new BigDecimal("14.99"))
                    .stock(999)
                    .category("Cristal génesis")
                    .active(true)
                    .build(),
                Product.builder()
                    .name("Cristal génesis ×1980")
                    .description("¡Doble! +1980")
                    .price(new BigDecimal("29.99"))
                    .stock(999)
                    .category("Cristal génesis")
                    .active(true)
                    .build(),
                Product.builder()
                    .name("Cristal génesis ×3280")
                    .description("¡Doble! +3280")
                    .price(new BigDecimal("49.99"))
                    .stock(999)
                    .category("Cristal génesis")
                    .active(true)
                    .build(),
                Product.builder()
                    .name("Cristal génesis ×6480")
                    .description("¡Doble! +6480")
                    .price(new BigDecimal("99.99"))
                    .stock(999)
                    .category("Cristal génesis")
                    .active(true)
                    .build()
            );

            productRepository.saveAll(products);
            log.info("Se han guardado {} productos de prueba.", products.size());
        }
    }
}
