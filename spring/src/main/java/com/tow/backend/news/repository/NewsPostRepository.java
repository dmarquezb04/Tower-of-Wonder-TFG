package com.tow.backend.news.repository;

import com.tow.backend.news.entity.NewsPost;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NewsPostRepository extends JpaRepository<NewsPost, Long> {

    /** Todas las noticias activas, ordenadas de más reciente a más antigua */
    List<NewsPost> findByActiveTrueOrderByPublishedAtDesc();

    /** Busca por slug para la vista de detalle */
    Optional<NewsPost> findBySlugAndActiveTrue(String slug);
}

