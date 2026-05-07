package com.tow.backend.user.repository;

import com.tow.backend.user.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio JPA para la entidad {@link Role}.
 *
 * @author DarÃ­o MÃ¡rquez Bautista
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    /**
     * Busca un rol por nombre. Usado al registrar un nuevo usuario
     * para asignarle el rol "user" por defecto.
     *
     * @param nombreRol nombre del rol (ej: "user", "admin")
     * @return Optional con el rol si existe
     */
    Optional<Role> findByNombreRol(String nombreRol);
}

