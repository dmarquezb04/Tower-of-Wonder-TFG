package com.tow.backend.character.controller;

import com.tow.backend.character.entity.CharacterImage;
import com.tow.backend.character.entity.GameCharacter;
import com.tow.backend.character.repository.GameCharacterRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controlador de Personajes.
 *
 * Endpoints públicos:
 *   GET /api/characters        → lista todos los personajes activos
 *   GET /api/characters/{slug} → detalle de un personaje por slug
 *
 * Endpoints admin (requieren JWT con rol ADMIN):
 *   GET    /api/characters/admin/all → todos (activos e inactivos)
 *   POST   /api/characters           → crear personaje (con su lista de imágenes)
 *   PUT    /api/characters/{id}      → actualizar personaje (reemplaza la lista de imágenes)
 *   DELETE /api/characters/{id}      → eliminar personaje (y sus imágenes por CASCADE)
 */
@RestController
@RequestMapping("/characters")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Personajes", description = "Gestión de personajes del juego")
public class CharacterController {

    private final GameCharacterRepository characterRepository;

    // ─── PÚBLICO ─────────────────────────────────────────────────────────────

    @GetMapping
    @Operation(summary = "Obtener todos los personajes activos")
    public ResponseEntity<List<GameCharacter>> getAll() {
        return ResponseEntity.ok(characterRepository.findByActiveTrueOrderByNameAsc());
    }

    @GetMapping("/{slug}")
    @Operation(summary = "Obtener un personaje por su slug")
    public ResponseEntity<GameCharacter> getBySlug(@PathVariable String slug) {
        return characterRepository.findBySlugAndActiveTrue(slug)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ─── ADMIN ────────────────────────────────────────────────────────────────

    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Obtener todos los personajes (admin, incluye inactivos)")
    public ResponseEntity<List<GameCharacter>> getAllAdmin() {
        return ResponseEntity.ok(characterRepository.findAll());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Crear un nuevo personaje con su lista de imágenes")
    public ResponseEntity<GameCharacter> create(@RequestBody GameCharacter data) {
        GameCharacter character = new GameCharacter();
        character.setName(data.getName());
        character.setSlug(data.getSlug() != null && !data.getSlug().isBlank()
                ? data.getSlug() : toSlug(data.getName()));
        character.setDescription(data.getDescription());
        character.setActive(data.getActive() != null ? data.getActive() : true);
        applyImages(character, data.getImages());

        GameCharacter saved = characterRepository.save(character);
        log.info("Personaje creado: {} (slug={})", saved.getName(), saved.getSlug());
        return ResponseEntity.status(201).body(saved);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Actualizar un personaje: reemplaza completamente su lista de imágenes")
    public ResponseEntity<GameCharacter> update(@PathVariable Long id,
                                                @RequestBody GameCharacter data) {
        return characterRepository.findById(id).map(character -> {
            character.setName(data.getName());
            if (data.getSlug() != null && !data.getSlug().isBlank()) {
                character.setSlug(data.getSlug());
            }
            character.setDescription(data.getDescription());
            if (data.getActive() != null) character.setActive(data.getActive());

            // orphanRemoval=true elimina las antiguas automáticamente
            character.getImages().clear();
            applyImages(character, data.getImages());

            return ResponseEntity.ok(characterRepository.save(character));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Eliminar un personaje (sus imágenes se eliminan por CASCADE)")
    public ResponseEntity<Map<String, String>> delete(@PathVariable Long id) {
        if (!characterRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        characterRepository.deleteById(id);
        log.info("Personaje eliminado: id={}", id);
        return ResponseEntity.ok(Map.of("message", "Personaje eliminado correctamente"));
    }

    // ─── UTILIDAD ─────────────────────────────────────────────────────────────

    /**
     * Asigna la lista de imágenes al personaje con sortOrder automático.
     */
    private void applyImages(GameCharacter character, List<CharacterImage> incoming) {
        if (incoming == null || incoming.isEmpty()) return;
        for (int i = 0; i < incoming.size(); i++) {
            CharacterImage img = new CharacterImage();
            img.setImageUrl(incoming.get(i).getImageUrl());
            img.setSortOrder(i);
            img.setCharacter(character);
            character.getImages().add(img);
        }
    }

    /** Genera un slug a partir del nombre: "Kyra la Valiente" → "kyra-la-valiente" */
    private String toSlug(String name) {
        if (name == null) return "";
        return name.toLowerCase()
                .replaceAll("[áàä]", "a").replaceAll("[éèë]", "e")
                .replaceAll("[íìï]", "i").replaceAll("[óòö]", "o")
                .replaceAll("[úùü]", "u").replaceAll("ñ", "n")
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-|-$)", "");
    }
}
