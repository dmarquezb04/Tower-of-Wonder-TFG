package com.tow.backend.character.controller;

import com.tow.backend.character.entity.CharacterImage;
import com.tow.backend.character.entity.GameCharacter;
import com.tow.backend.character.repository.GameCharacterRepository;
import com.tow.backend.common.dto.ApiResponse;
import com.tow.backend.exception.NotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para la gestiÃ³n de los personajes del juego.
 *
 * <p>Los endpoints de lectura son pÃºblicos. Las operaciones de escritura
 * (crear, actualizar, eliminar) requieren el rol ADMIN.
 * Los errores de negocio son gestionados por {@link com.tow.backend.exception.GlobalExceptionHandler}.
 *
 * @author DarÃ­o MÃ¡rquez Bautista
 */
@RestController
@RequestMapping("/characters")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Personajes", description = "CatÃ¡logo de personajes y gestiÃ³n (Admin)")
public class CharacterController {

    private final GameCharacterRepository characterRepository;

    // â”€â”€â”€ PÃšBLICO â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    /**
     * Devuelve la lista de todos los personajes que estÃ¡n marcados como activos.
     *
     * @return 200 OK con lista de personajes
     */
    @GetMapping
    @Operation(summary = "Obtener todos los personajes activos")
    public ResponseEntity<List<GameCharacter>> getAll() {
        return ResponseEntity.ok(characterRepository.findByActiveTrueOrderByNameAsc());
    }

    /**
     * Devuelve el detalle de un personaje especÃ­fico a partir de su slug.
     *
     * @param slug identificador amigable del personaje (ej. "kyra-la-valiente")
     * @return 200 OK con el detalle del personaje
     * @throws NotFoundException si el slug no corresponde a ningÃºn personaje activo
     */
    @GetMapping("/{slug}")
    @Operation(summary = "Obtener un personaje por su slug")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Personaje encontrado"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Personaje no encontrado")
    })
    public ResponseEntity<GameCharacter> getBySlug(@PathVariable String slug) {
        return characterRepository.findBySlugAndActiveTrue(slug)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new NotFoundException("Personaje no encontrado con slug: " + slug));
    }

    // â”€â”€â”€ ADMIN â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    /**
     * Devuelve la lista completa de personajes, incluyendo los inactivos.
     *
     * @return 200 OK con lista de personajes
     */
    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Obtener todos los personajes (incluye inactivos)")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Lista devuelta correctamente")
    public ResponseEntity<List<GameCharacter>> getAllAdmin() {
        return ResponseEntity.ok(characterRepository.findAll());
    }

    /**
     * Crea un nuevo personaje con su galerÃ­a de imÃ¡genes.
     *
     * @param data datos del nuevo personaje
     * @return 201 Created con el personaje guardado
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Crear un nuevo personaje")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Personaje creado correctamente")
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
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    /**
     * Actualiza los datos y la galerÃ­a de un personaje existente.
     *
     * @param id   ID del personaje a actualizar
     * @param data nuevos datos
     * @return 200 OK con el personaje actualizado
     * @throws NotFoundException si el ID no existe
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Actualizar un personaje existente")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Personaje actualizado correctamente"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Personaje no encontrado")
    })
    public ResponseEntity<GameCharacter> update(@PathVariable Long id, @RequestBody GameCharacter data) {
        GameCharacter character = characterRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Personaje no encontrado con ID: " + id));

        character.setName(data.getName());
        if (data.getSlug() != null && !data.getSlug().isBlank()) {
            character.setSlug(data.getSlug());
        }
        character.setDescription(data.getDescription());
        if (data.getActive() != null) character.setActive(data.getActive());

        character.getImages().clear();
        applyImages(character, data.getImages());

        return ResponseEntity.ok(characterRepository.save(character));
    }

    /**
     * Elimina permanentemente un personaje del sistema.
     *
     * @param id ID del personaje a eliminar
     * @return 200 OK con mensaje de confirmaciÃ³n
     * @throws NotFoundException si el ID no existe
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Eliminar un personaje")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Personaje eliminado correctamente"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Personaje no encontrado")
    })
    public ResponseEntity<ApiResponse> delete(@PathVariable Long id) {
        if (!characterRepository.existsById(id)) {
            throw new NotFoundException("Personaje no encontrado con ID: " + id);
        }
        characterRepository.deleteById(id);
        log.info("Personaje eliminado: id={}", id);
        return ResponseEntity.ok(new ApiResponse("Personaje eliminado correctamente"));
    }

    // â”€â”€â”€ UTILIDAD â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

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

    private String toSlug(String name) {
        if (name == null) return "";
        return name.toLowerCase()
                .replaceAll("[Ã¡Ã Ã¤]", "a").replaceAll("[Ã©Ã¨Ã«]", "e")
                .replaceAll("[Ã­Ã¬Ã¯]", "i").replaceAll("[Ã³Ã²Ã¶]", "o")
                .replaceAll("[ÃºÃ¹Ã¼]", "u").replaceAll("Ã±", "n")
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-|-$)", "");
    }
}

