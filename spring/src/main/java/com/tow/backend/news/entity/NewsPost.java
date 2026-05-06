package com.tow.backend.news.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entidad que representa una noticia / post del blog.
 */
@Entity
@Table(name = "news_posts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NewsPost {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String title;

    /** Slug único para URL amigable, ej: "actualizacion-1-2" */
    @Column(nullable = false, unique = true, length = 255)
    private String slug;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    /** Resumen corto para la lista de noticias (tarjetas) */
    @Column(length = 500)
    private String summary;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @Column(nullable = false)
    private Boolean active = true;

    @PrePersist
    protected void onCreate() {
        if (publishedAt == null) {
            publishedAt = LocalDateTime.now();
        }
    }
}
