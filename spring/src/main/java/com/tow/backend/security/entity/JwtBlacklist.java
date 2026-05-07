package com.tow.backend.security.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Entidad JPA que mapea la tabla {@code jwt_blacklist}.
 *
 * <p>Almacena los JWT revocados (tokens de usuarios que han hecho logout).
 * Al validar un token, se comprueba que su {@code jti} no estÃ© en esta tabla.
 *
 * <p>La columna {@code fecha_expiracion} permite limpiar periÃ³dicamente la
 * tabla eliminando tokens que ya habrÃ­an expirado de todas formas.
 *
 * @author DarÃ­o MÃ¡rquez Bautista
 * @see com.tow.backend.security.JwtTokenProvider
 */
@Entity
@Table(name = "jwt_blacklist")
@Getter
@Setter
@NoArgsConstructor
public class JwtBlacklist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** ID Ãºnico del token JWT (claim 'jti'). */
    @Column(name = "token_jti", nullable = false, unique = true, length = 64)
    private String tokenJti;

    /** Fecha de expiraciÃ³n original del token (para limpieza de la tabla). */
    @Column(name = "fecha_expiracion", nullable = false)
    private LocalDateTime fechaExpiracion;

    /** CuÃ¡ndo se aÃ±adiÃ³ a la blacklist (momento del logout). */
    @Column(name = "fecha_revocacion")
    private LocalDateTime fechaRevocacion;

    @PrePersist
    protected void onCreate() {
        if (fechaRevocacion == null) {
            fechaRevocacion = LocalDateTime.now();
        }
    }
}

