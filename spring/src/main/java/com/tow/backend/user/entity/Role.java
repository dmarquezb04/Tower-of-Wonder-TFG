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
 *   <li>{@code admin} — acceso total al panel de administración</li>
 *   <li>{@code moderator} — permisos especiales de moderación</li>
 *   <li>{@code user} — usuario normal del sistema</li>
 * </ul>
 *
 * @author Darío Márquez Bautista
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
     * Nombre del rol. Spring Security lo leerá como autoridad.
     * Formato en BD: {@code admin}, {@code user}, {@code moderator}.
     * Spring Security prefiere el prefijo {@code ROLE_}, que se añade
     * en {@link com.tow.backend.security.UserDetailsServiceImpl}.
     */
    @Column(name = "nombre_rol", nullable = false, unique = true, length = 50)
    private String nombreRol;

    @Column(length = 255)
    private String descripcion;
}

