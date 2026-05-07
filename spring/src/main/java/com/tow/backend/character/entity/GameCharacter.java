package com.tow.backend.character.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Entidad que representa un personaje del videojuego.
 * Las imÃ¡genes se gestionan en la tabla character_images (lista dinÃ¡mica, sin lÃ­mite).
 */
@Entity
@Table(name = "game_characters")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GameCharacter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    /** Identificador amigable para URL, ej: "kyra" â†’ /personajes#kyra */
    @Column(nullable = false, unique = true, length = 100)
    private String slug;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private Boolean active = true;

    /**
     * Lista de imÃ¡genes del personaje, ordenadas por sortOrder.
     * CascadeType.ALL + orphanRemoval = true permite gestionar la lista
     * directamente desde el personaje (aÃ±adir, eliminar, reordenar).
     */
    @OneToMany(mappedBy = "character", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @OrderBy("sortOrder ASC")
    @Builder.Default
    private List<CharacterImage> images = new ArrayList<>();
}

