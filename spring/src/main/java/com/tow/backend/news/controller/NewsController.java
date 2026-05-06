package com.tow.backend.news.controller;

import com.tow.backend.news.entity.NewsPost;
import com.tow.backend.news.repository.NewsPostRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Controlador de Noticias / Blog.
 *
 * Endpoints públicos:
 *   GET /api/news           → lista todos los posts activos (más recientes primero)
 *   GET /api/news/{slug}    → post completo por slug
 *
 * Endpoints admin (requieren JWT con rol ADMIN):
 *   POST   /api/news             → crear post
 *   PUT    /api/news/{id}        → actualizar post
 *   DELETE /api/news/{id}        → eliminar post
 *   GET    /api/news/admin/all   → todos los posts (activos e inactivos)
 */
@RestController
@RequestMapping("/news")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Noticias", description = "Blog de noticias del juego")
public class NewsController {

    private final NewsPostRepository newsPostRepository;

    // ─── PÚBLICO ─────────────────────────────────────────────────────────────

    @GetMapping
    @Operation(summary = "Obtener todos los posts activos")
    public ResponseEntity<List<NewsPost>> getAll() {
        return ResponseEntity.ok(newsPostRepository.findByActiveTrueOrderByPublishedAtDesc());
    }

    @GetMapping("/{slug}")
    @Operation(summary = "Obtener un post por su slug")
    public ResponseEntity<NewsPost> getBySlug(@PathVariable String slug) {
        return newsPostRepository.findBySlugAndActiveTrue(slug)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ─── ADMIN ────────────────────────────────────────────────────────────────

    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Obtener todos los posts (admin, incluye inactivos)")
    public ResponseEntity<List<NewsPost>> getAllAdmin() {
        return ResponseEntity.ok(newsPostRepository.findAll());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Crear un nuevo post")
    public ResponseEntity<NewsPost> create(@RequestBody NewsPost post) {
        if (post.getSlug() == null || post.getSlug().isBlank()) {
            post.setSlug(toSlug(post.getTitle()));
        }
        if (post.getPublishedAt() == null) {
            post.setPublishedAt(LocalDateTime.now());
        }
        NewsPost saved = newsPostRepository.save(post);
        log.info("Post creado: {} (slug={})", saved.getTitle(), saved.getSlug());
        return ResponseEntity.status(201).body(saved);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Actualizar un post existente")
    public ResponseEntity<NewsPost> update(@PathVariable Long id,
                                           @RequestBody NewsPost data) {
        return newsPostRepository.findById(id).map(post -> {
            post.setTitle(data.getTitle());
            if (data.getSlug() != null && !data.getSlug().isBlank()) {
                post.setSlug(data.getSlug());
            }
            post.setContent(data.getContent());
            post.setSummary(data.getSummary());
            post.setImageUrl(data.getImageUrl());
            post.setActive(data.getActive());
            return ResponseEntity.ok(newsPostRepository.save(post));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Eliminar un post")
    public ResponseEntity<Map<String, String>> delete(@PathVariable Long id) {
        if (!newsPostRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        newsPostRepository.deleteById(id);
        log.info("Post eliminado: id={}", id);
        return ResponseEntity.ok(Map.of("message", "Post eliminado correctamente"));
    }

    // ─── UTILIDAD ─────────────────────────────────────────────────────────────

    private String toSlug(String title) {
        if (title == null) return "";
        return title.toLowerCase()
                .replaceAll("[áàä]", "a").replaceAll("[éèë]", "e")
                .replaceAll("[íìï]", "i").replaceAll("[óòö]", "o")
                .replaceAll("[úùü]", "u").replaceAll("ñ", "n")
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-|-$)", "");
    }
}
