package com.tow.backend.security.repository;

import com.tow.backend.security.entity.JwtBlacklist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Repositorio JPA para la tabla {@code jwt_blacklist}.
 *
 * @author Darío Márquez Bautista
 */
@Repository
public interface JwtBlacklistRepository extends JpaRepository<JwtBlacklist, Long> {

    /**
     * Comprueba si un token (por su JTI) ha sido revocado.
     * Se llama en cada petición autenticada para verificar que el token
     * no fue invalidado por un logout anterior.
     *
     * @param tokenJti el claim 'jti' del token JWT
     * @return true si el token está en la blacklist (revocado)
     */
    boolean existsByTokenJti(String tokenJti);

    /**
     * Elimina los tokens cuya fecha de expiración ya ha pasado.
     * Puede ejecutarse periódicamente para mantener la tabla limpia.
     *
     * @param now fecha/hora actual
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM JwtBlacklist j WHERE j.fechaExpiracion < :now")
    void deleteExpiredTokens(LocalDateTime now);
}
