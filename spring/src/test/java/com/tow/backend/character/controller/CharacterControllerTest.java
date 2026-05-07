package com.tow.backend.character.controller;

import com.tow.backend.character.entity.CharacterImage;
import com.tow.backend.character.entity.GameCharacter;
import com.tow.backend.character.repository.GameCharacterRepository;
import com.tow.backend.common.dto.ApiResponse;
import com.tow.backend.exception.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CharacterControllerTest {

    @Mock
    private GameCharacterRepository characterRepository;

    @InjectMocks
    private CharacterController characterController;

    private GameCharacter character;

    @BeforeEach
    void setUp() {
        character = new GameCharacter();
        character.setId(1L);
        character.setName("Test Character");
        character.setSlug("test-character");
        character.setActive(true);
        character.setImages(new ArrayList<>());
    }

    @Test
    void getAll_ReturnsActiveCharacters() {
        when(characterRepository.findByActiveTrueOrderByIdAsc()).thenReturn(Arrays.asList(character));

        ResponseEntity<List<GameCharacter>> response = characterController.getAll();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        assertEquals("Test Character", response.getBody().get(0).getName());
    }

    @Test
    void getBySlug_Success() {
        when(characterRepository.findBySlugAndActiveTrue("test-character")).thenReturn(Optional.of(character));

        ResponseEntity<GameCharacter> response = characterController.getBySlug("test-character");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Test Character", response.getBody().getName());
    }

    @Test
    void getBySlug_NotFound_ThrowsException() {
        when(characterRepository.findBySlugAndActiveTrue("not-found")).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> characterController.getBySlug("not-found"));
    }

    @Test
    void getAllAdmin_ReturnsAllCharacters() {
        when(characterRepository.findAll()).thenReturn(Arrays.asList(character));

        ResponseEntity<List<GameCharacter>> response = characterController.getAllAdmin();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void create_GeneratesSlugAndSaves() {
        GameCharacter input = new GameCharacter();
        input.setName("New Hero áéíóú"); // Test slug generation
        input.setActive(true);
        
        CharacterImage img = new CharacterImage();
        img.setImageUrl("test.png");
        input.setImages(List.of(img));

        when(characterRepository.save(any(GameCharacter.class))).thenAnswer(i -> {
            GameCharacter saved = i.getArgument(0);
            saved.setId(2L);
            return saved;
        });

        ResponseEntity<GameCharacter> response = characterController.create(input);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        GameCharacter created = response.getBody();
        assertEquals("new-hero-aeiou", created.getSlug());
        assertEquals(1, created.getImages().size());
        assertEquals(0, created.getImages().get(0).getSortOrder());
        verify(characterRepository).save(any(GameCharacter.class));
    }

    @Test
    void update_Success() {
        GameCharacter input = new GameCharacter();
        input.setName("Updated Name");
        input.setSlug("updated-slug");
        
        when(characterRepository.findById(1L)).thenReturn(Optional.of(character));
        when(characterRepository.save(any(GameCharacter.class))).thenReturn(character);

        ResponseEntity<GameCharacter> response = characterController.update(1L, input);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Updated Name", character.getName());
        assertEquals("updated-slug", character.getSlug());
        verify(characterRepository).save(character);
    }

    @Test
    void update_NotFound_ThrowsException() {
        when(characterRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> characterController.update(99L, new GameCharacter()));
        verify(characterRepository, never()).save(any());
    }

    @Test
    void delete_Success() {
        when(characterRepository.existsById(1L)).thenReturn(true);

        ResponseEntity<ApiResponse> response = characterController.delete(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Personaje eliminado correctamente", response.getBody().getMessage());
        verify(characterRepository).deleteById(1L);
    }

    @Test
    void delete_NotFound_ThrowsException() {
        when(characterRepository.existsById(99L)).thenReturn(false);

        assertThrows(NotFoundException.class, () -> characterController.delete(99L));
        verify(characterRepository, never()).deleteById(any());
    }
}
