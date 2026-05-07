package com.tow.backend.security;

import com.tow.backend.user.entity.Role;
import com.tow.backend.user.entity.User;
import com.tow.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * ImplementaciÃ³n de {@link UserDetailsService} que carga usuarios desde la BD.
 *
 * <p>Spring Security llama a este servicio durante el proceso de autenticaciÃ³n
 * para obtener los datos del usuario (contraseÃ±a y roles) y verificar las credenciales.
 *
 * <p>El "username" en Spring Security es el email del usuario (campo {@code email}
 * en la tabla {@code usuarios}).
 *
 * @author DarÃ­o MÃ¡rquez Bautista
 */
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * Carga un usuario por su email para que Spring Security pueda
     * verificar la contraseÃ±a y los roles.
     *
     * @param email email del usuario (usado como username)
     * @return UserDetails con email, passwordHash y roles
     * @throws UsernameNotFoundException si el usuario no existe o estÃ¡ inactivo
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Usuario no encontrado: " + email
                ));

        if (!Boolean.TRUE.equals(user.getActivo())) {
            throw new UsernameNotFoundException("Cuenta desactivada: " + email);
        }

        List<GrantedAuthority> authorities = mapRolesToAuthorities(user.getRole());

        // Spring Security requiere un objeto UserDetails.
        // Usamos el builder estÃ¡ndar de Spring.
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPasswordHash())
                .authorities(authorities)
                .build();
    }

    /**
     * Convierte los roles de la BD al formato que entiende Spring Security.
     *
     * <p>Spring Security requiere que los roles tengan el prefijo {@code ROLE_}
     * para funcionar con {@code @PreAuthorize("hasRole('ADMIN')")} etc.
     *
     * <p>Ejemplo: {@code "admin"} â†’ {@code "ROLE_ADMIN"}
     *
     * @param roles conjunto de roles del usuario
     * @return autoridades Spring Security
     */
    private List<GrantedAuthority> mapRolesToAuthorities(Role role) {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.getNombreRol().toUpperCase()));
    }
}

