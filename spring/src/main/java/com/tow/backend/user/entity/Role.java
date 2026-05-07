package com.tow.backend.user.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

/**
 * Entidad JPA que mapea la tabla {@code roles}.
 *
 * <p>Los tres roles predefinidos en la BD son:
 * <ul>
 *   <li>{@code admin} â€” acceso total al panel de administraciÃ³n</li>
 *   <li>{@code moderator} â€” permisos especiales de moderaciÃ³n</li>
 *   <li>{@code user} â€” usuario normal del sistema</li>
 * </ul>
 *
 * @author DarÃ­o MÃ¡rquez Bautista
 */
@Entity
@Table(name = "roles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_rol")
    private Long idRol;

    /**
     * Nombre del rol. Spring Security lo leerÃ¡ como autoridad.
     * Formato en BD: {@code admin}, {@code user}, {@code moderator}.
     * Spring Security prefiere el prefijo {@code ROLE_}, que se aÃ±ade
     * en {@link com.tow.backend.security.UserDetailsServiceImpl}.
     */
    @Column(name = "nombre_rol", nullable = false, unique = true, length = 50)
    private String nombreRol;

    @Column(length = 255)
    private String descripcion;
}

