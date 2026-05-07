package com.tow.backend.news.controller;

import com.tow.backend.common.dto.ApiResponse;
import com.tow.backend.exception.NotFoundException;
import com.tow.backend.news.entity.NewsPost;
import com.tow.backend.news.repository.NewsPostRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Controlador REST para el blog de noticias de Tower of Wonder.
 *
 * <p>Los endpoints de lectura son pÃºblicos. Las operaciones de escritura
 * (crear, actualizar, eliminar) requieren el rol ADMIN.
 * Los errores de negocio son gestionados por {@link com.tow.backend.exception.GlobalExceptionHandler}.
 *
 * @author DarÃ­o MÃ¡rquez Bautista
 */
@RestController
@RequestMapping("/news")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Noticias", description = "Blog de noticias y anuncios (Admin)")
public class NewsController {

    private final NewsPostRepository newsPostRepository;

    // â”€â”€â”€ PÃšBLICO â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    /**
     * Devuelve la lista de noticias marcadas como activas, ordenadas por fecha (mÃ¡s reciente primero).
     *
     * @return 200 OK con lista de noticias
     */
    @GetMapping
    @Operation(summary = "Obtener noticias activas")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Lista devuelta correctamente")
    public ResponseEntity<List<NewsPost>> getAll() {
        return ResponseEntity.ok(newsPostRepository.findByActiveTrueOrderByPublishedAtDesc());
    }

    /**
     * Devuelve el detalle de una noticia a partir de su slug.
     *
     * @param slug identificador amigable de la noticia
     * @return 200 OK con el detalle de la noticia
     * @throws NotFoundException si el slug no corresponde a ninguna noticia activa
     */
    @GetMapping("/{slug}")
    @Operation(summary = "Obtener una noticia por su slug")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Noticia encontrada"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Noticia no encontrada")
    })
    public ResponseEntity<NewsPost> getBySlug(@PathVariable String slug) {
        return newsPostRepository.findBySlugAndActiveTrue(slug)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new NotFoundException("Noticia no encontrada con slug: " + slug));
    }

    // â”€â”€â”€ ADMIN â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    /**
     * Devuelve la lista completa de noticias para administraciÃ³n.
     *
     * @return 200 OK con lista de todas las noticias
     */
    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Obtener todas las noticias (incluye inactivas)")
    public ResponseEntity<List<NewsPost>> getAllAdmin() {
        return ResponseEntity.ok(newsPostRepository.findAll());
    }

    /**
     * Crea una nueva noticia.
     *
     * @param post datos de la noticia
     * @return 201 Created con la noticia guardada
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Crear una nueva noticia")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Noticia creada correctamente")
    public ResponseEntity<NewsPost> create(@RequestBody NewsPost post) {
        if (post.getSlug() == null || post.getSlug().isBlank()) {
            post.setSlug(toSlug(post.getTitle()));
        }
        if (post.getPublishedAt() == null) {
            post.setPublishedAt(LocalDateTime.now());
        }
        NewsPost saved = newsPostRepository.save(post);
        log.info("Noticia creada: {} (slug={})", saved.getTitle(), saved.getSlug());
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    /**
     * Actualiza una noticia existente.
     *
     * @param id   ID de la noticia
     * @param data nuevos datos
     * @return 200 OK con la noticia actualizada
     * @throws NotFoundException si el ID no existe
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Actualizar una noticia existente")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Noticia actualizada correctamente"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Noticia no encontrada")
    })
    public ResponseEntity<NewsPost> update(@PathVariable Long id, @RequestBody NewsPost data) {
        NewsPost post = newsPostRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Noticia no encontrada con ID: " + id));

        post.setTitle(data.getTitle());
        if (data.getSlug() != null && !data.getSlug().isBlank()) {
            post.setSlug(data.getSlug());
        }
        post.setContent(data.getContent());
        post.setSummary(data.getSummary());
        post.setImageUrl(data.getImageUrl());
        post.setActive(data.getActive());

        return ResponseEntity.ok(newsPostRepository.save(post));
    }

    /**
     * Elimina una noticia del sistema.
     *
     * @param id ID de la noticia a eliminar
     * @return 200 OK con mensaje de confirmaciÃ³n
     * @throws NotFoundException si el ID no existe
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Eliminar una noticia")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Noticia eliminada correctamente"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Noticia no encontrada")
    })
    public ResponseEntity<ApiResponse> delete(@PathVariable Long id) {
        if (!newsPostRepository.existsById(id)) {
            throw new NotFoundException("Noticia no encontrada con ID: " + id);
        }
        newsPostRepository.deleteById(id);
        log.info("Noticia eliminada: id={}", id);
        return ResponseEntity.ok(new ApiResponse("Noticia eliminada correctamente"));
    }

    // â”€â”€â”€ UTILIDAD â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    private String toSlug(String title) {
        if (title == null) return "";
        return title.toLowerCase()
                .replaceAll("[Ã¡Ã Ã¤]", "a").replaceAll("[Ã©Ã¨Ã«]", "e")
                .replaceAll("[Ã­Ã¬Ã¯]", "i").replaceAll("[Ã³Ã²Ã¶]", "o")
                .replaceAll("[ÃºÃ¹Ã¼]", "u").replaceAll("Ã±", "n")
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-|-$)", "");
    }
}

