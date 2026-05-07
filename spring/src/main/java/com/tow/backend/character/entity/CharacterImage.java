package com.tow.backend.character.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Imagen de un personaje. Permite cualquier nÃºmero de imÃ¡genes por personaje.
 * ON DELETE CASCADE: si se elimina el personaje, sus imÃ¡genes se eliminan automÃ¡ticamente.
 */
@Entity
@Table(name = "character_images")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CharacterImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "character_id", nullable = false)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private GameCharacter character;

    @Column(name = "image_url", nullable = false)
    private String imageUrl;

    /** PosiciÃ³n en el carrusel (0 = primera) */
    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;
}

