package com.tow.backend.user.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;


/**
 * Entidad JPA que mapea la tabla {@code usuarios} de la base de datos.
 *
 * <p>Corresponde al modelo {@code Usuario.php} del backend PHP legacy.
 * Los hashes de contraseÃ±a son BCrypt y compatibles con
 * {@link org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder}.
 *
 * @author DarÃ­o MÃ¡rquez Bautista
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

    /** Email Ãºnico de acceso â€” usado como username en Spring Security. */
    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(length = 50)
    private String username;

    /**
     * Hash BCrypt de la contraseÃ±a. Generado con password_hash() en PHP o
     * BCryptPasswordEncoder en Spring. Ambos son compatibles.
     * NUNCA se expone en respuestas API.
     */
    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    /** Si el 2FA (Google Authenticator) estÃ¡ activo para este usuario. */
    @Column(name = "two_fa_enabled")
    private Boolean twoFaEnabled = false;

    /**
     * Secreto Base32 para TOTP (Google Authenticator).
     * Compatible con PHPGangsta_GoogleAuthenticator y com.warrenstrange:googleauth.
     */
    @Column(name = "twofa_secret", length = 255)
    private String twofaSecret;

    /** Si la cuenta estÃ¡ activa. Las cuentas desactivadas no pueden iniciar sesiÃ³n. */
    @Column(nullable = false)
    private Boolean activo = true;

    /** Token de recuperaciÃ³n para reactivar cuentas borradas lÃ³gicamente. */
    @Column(name = "recovery_token", length = 100)
    private String recoveryToken;

    /** Fecha de expiraciÃ³n del token de recuperaciÃ³n (normalmente 7 dÃ­as). */
    @Column(name = "recovery_token_expiry")
    private LocalDateTime recoveryTokenExpiry;

    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;

    @Column(name = "ultimo_login")
    private LocalDateTime ultimoLogin;

    /**
     * Roles del usuario. RelaciÃ³n ManyToMany con la tabla {@code roles}
     * a travÃ©s de la tabla intermedia {@code usuario_roles}.
     * EAGER: se cargan siempre (necesario para Spring Security).
     */
    /**
     * Rol del usuario. RelaciÃ³n ManyToOne con la tabla {@code roles}.
     * Cada usuario tiene asignado exactamente un rol.
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_rol", nullable = true)
    private Role role;

    /** Establece la fecha de creaciÃ³n automÃ¡ticamente antes de persistir. */
    @PrePersist
    protected void onCreate() {
        if (fechaCreacion == null) {
            fechaCreacion = LocalDateTime.now();
        }
    }
}

