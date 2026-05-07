package com.tow.backend.user.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;


/**
 * Entidad JPA que mapea la tabla {@code usuarios} de la base de datos.
 *
 * <p>Corresponde al modelo {@code Usuario.php} del backend PHP legacy.
 * Los hashes de contraseña son BCrypt y compatibles con
 * {@link org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder}.
 *
 * @author Darío Márquez Bautista
 */
@Entity
@Table(name = "usuarios")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario")
    private Long idUsuario;

    /** Email único de acceso — usado como username en Spring Security. */
    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(length = 50)
    private String username;

    /**
     * Hash BCrypt de la contraseña. Generado con password_hash() en PHP o
     * BCryptPasswordEncoder en Spring. Ambos son compatibles.
     * NUNCA se expone en respuestas API.
     */
    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    /** Si el 2FA (Google Authenticator) está activo para este usuario. */
    @Column(name = "two_fa_enabled")
    @Builder.Default
    private Boolean twoFaEnabled = false;

    /**
     * Secreto Base32 para TOTP (Google Authenticator).
     * Compatible con PHPGangsta_GoogleAuthenticator y com.warrenstrange:googleauth.
     */
    @Column(name = "twofa_secret", length = 255)
    private String twofaSecret;

    /** Si la cuenta está activa. Las cuentas desactivadas no pueden iniciar sesión. */
    @Column(nullable = false)
    @Builder.Default
    private Boolean activo = true;

    /** Token de recuperación para reactivar cuentas borradas lógicamente. */
    @Column(name = "recovery_token", length = 100)
    private String recoveryToken;

    /** Fecha de expiración del token de recuperación (normalmente 7 días). */
    @Column(name = "recovery_token_expiry")
    private LocalDateTime recoveryTokenExpiry;

    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;

    @Column(name = "ultimo_login")
    private LocalDateTime ultimoLogin;

    /**
     * Roles del usuario. Relación ManyToMany con la tabla {@code roles}
     * a través de la tabla intermedia {@code usuario_roles}.
     * EAGER: se cargan siempre (necesario para Spring Security).
     */
    /**
     * Rol del usuario. Relación ManyToOne con la tabla {@code roles}.
     * Cada usuario tiene asignado exactamente un rol.
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_rol", nullable = true)
    private Role role;

    /** Establece la fecha de creación automáticamente antes de persistir. */
    @PrePersist
    protected void onCreate() {
        if (fechaCreacion == null) {
            fechaCreacion = LocalDateTime.now();
        }
    }
}

