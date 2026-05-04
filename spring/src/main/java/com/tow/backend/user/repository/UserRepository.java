package com.tow.backend.user.repository;

import com.tow.backend.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio JPA para la entidad {@link User}.
 *
 * <p>Spring Data JPA genera automáticamente la implementación de estos métodos
 * en tiempo de compilación, sin necesidad de escribir SQL manualmente.
 *
 * @author Darío Márquez Bautista
 */
@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

    /**
     * Busca un usuario por email. Equivale al método
     * {@code Usuario::findByEmail()} del modelo PHP.
     *
     * @param email email del usuario
     * @return Optional con el usuario si existe
     */
    Optional<User> findByEmail(String email);

    /**
     * Comprueba si ya existe un usuario con ese email.
     * Más eficiente que {@link #findByEmail} cuando solo se necesita saber si existe.
     *
     * @param email email a comprobar
     * @return true si ya está registrado
     */
    boolean existsByEmail(String email);

    /**
     * Comprueba si ya existe un usuario con ese username.
     *
     * @param username nombre de usuario a comprobar
     * @return true si ya está en uso
     */
    boolean existsByUsername(String username);

    /**
     * Busca un usuario por su token de recuperación de cuenta.
     *
     * @param recoveryToken el token generado al borrar la cuenta
     * @return Optional con el usuario
     */
    Optional<User> findByRecoveryToken(String recoveryToken);
}
