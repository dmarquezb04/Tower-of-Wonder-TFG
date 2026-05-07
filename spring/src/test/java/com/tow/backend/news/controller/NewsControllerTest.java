package com.tow.backend.news.controller;

import com.tow.backend.common.dto.ApiResponse;
import com.tow.backend.exception.NotFoundException;
import com.tow.backend.news.entity.NewsPost;
import com.tow.backend.news.repository.NewsPostRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NewsControllerTest {

    @Mock
    private NewsPostRepository newsPostRepository;

    @InjectMocks
    private NewsController newsController;

    private NewsPost post;

    @BeforeEach
    void setUp() {
        post = new NewsPost();
        post.setId(1L);
        post.setTitle("Test News");
        post.setSlug("test-news");
        post.setPublishedAt(LocalDateTime.now());
        post.setActive(true);
    }

    @Test
    void getAll_ReturnsActivePosts() {
        when(newsPostRepository.findByActiveTrueOrderByPublishedAtDesc()).thenReturn(Arrays.asList(post));

        ResponseEntity<List<NewsPost>> response = newsController.getAll();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        assertEquals("Test News", response.getBody().get(0).getTitle());
    }

    @Test
    void getBySlug_Success() {
        when(newsPostRepository.findBySlugAndActiveTrue("test-news")).thenReturn(Optional.of(post));

        ResponseEntity<NewsPost> response = newsController.getBySlug("test-news");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Test News", response.getBody().getTitle());
    }

    @Test
    void getBySlug_NotFound_ThrowsException() {
        when(newsPostRepository.findBySlugAndActiveTrue("not-found")).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> newsController.getBySlug("not-found"));
    }

    @Test
    void getAllAdmin_ReturnsAllPosts() {
        when(newsPostRepository.findAll()).thenReturn(Arrays.asList(post));

        ResponseEntity<List<NewsPost>> response = newsController.getAllAdmin();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void create_GeneratesSlugAndSaves() {
        NewsPost input = new NewsPost();
        input.setTitle("New Update ñ!");
        input.setActive(true);

        when(newsPostRepository.save(any(NewsPost.class))).thenAnswer(i -> {
            NewsPost saved = i.getArgument(0);
            saved.setId(2L);
            return saved;
        });

        ResponseEntity<NewsPost> response = newsController.create(input);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        NewsPost created = response.getBody();
        assertEquals("new-update-n", created.getSlug());
        assertNotNull(created.getPublishedAt());
        verify(newsPostRepository).save(any(NewsPost.class));
    }

    @Test
    void update_Success() {
        NewsPost input = new NewsPost();
        input.setTitle("Updated Title");
        input.setSlug("updated-slug");

        when(newsPostRepository.findById(1L)).thenReturn(Optional.of(post));
        when(newsPostRepository.save(any(NewsPost.class))).thenReturn(post);

        ResponseEntity<NewsPost> response = newsController.update(1L, input);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Updated Title", post.getTitle());
        assertEquals("updated-slug", post.getSlug());
        verify(newsPostRepository).save(post);
    }

    @Test
    void update_NotFound_ThrowsException() {
        when(newsPostRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> newsController.update(99L, new NewsPost()));
        verify(newsPostRepository, never()).save(any());
    }

    @Test
    void delete_Success() {
        when(newsPostRepository.existsById(1L)).thenReturn(true);

        ResponseEntity<ApiResponse> response = newsController.delete(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Noticia eliminada correctamente", response.getBody().getMessage());
        verify(newsPostRepository).deleteById(1L);
    }

    @Test
    void delete_NotFound_ThrowsException() {
        when(newsPostRepository.existsById(99L)).thenReturn(false);

        assertThrows(NotFoundException.class, () -> newsController.delete(99L));
        verify(newsPostRepository, never()).deleteById(any());
    }
}
