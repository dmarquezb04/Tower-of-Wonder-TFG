package com.tow.backend.user.repository;

import com.tow.backend.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio JPA para la entidad {@link User}.
 *
 * <p>Spring Data JPA genera automÃ¡ticamente la implementaciÃ³n de estos mÃ©todos
 * en tiempo de compilaciÃ³n, sin necesidad de escribir SQL manualmente.
 *
 * @author DarÃ­o MÃ¡rquez Bautista
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Busca un usuario por email. Equivale al mÃ©todo
     * {@code Usuario::findByEmail()} del modelo PHP.
     *
     * @param email email del usuario
     * @return Optional con el usuario si existe
     */
    Optional<User> findByEmail(String email);

    /**
     * Comprueba si ya existe un usuario con ese email.
     * MÃ¡s eficiente que {@link #findByEmail} cuando solo se necesita saber si existe.
     *
     * @param email email a comprobar
     * @return true si ya estÃ¡ registrado
     */
    boolean existsByEmail(String email);

    /**
     * Comprueba si ya existe un usuario con ese username.
     *
     * @param username nombre de usuario a comprobar
     * @return true si ya estÃ¡ en uso
     */
    boolean existsByUsername(String username);

    /**
     * Busca un usuario por su token de recuperaciÃ³n de cuenta.
     *
     * @param recoveryToken el token generado al borrar la cuenta
     * @return Optional con el usuario
     */
    Optional<User> findByRecoveryToken(String recoveryToken);
}

