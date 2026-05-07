package com.tow.backend.character.repository;

import com.tow.backend.character.entity.GameCharacter;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GameCharacterRepository extends JpaRepository<GameCharacter, Long> {

    /** Todos los personajes activos (para el menú y la página pública), ordenados por ID ASC (más nuevos abajo) */
    List<GameCharacter> findByActiveTrueOrderByIdAsc();

    /** Busca por slug para la vista de detalle */
    Optional<GameCharacter> findBySlugAndActiveTrue(String slug);
}

