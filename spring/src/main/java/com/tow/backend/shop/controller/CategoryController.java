package com.tow.backend.shop.controller;

import com.tow.backend.shop.entity.Category;
import com.tow.backend.shop.repository.CategoryRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Categorías", description = "Gestión de categorías de productos")
public class CategoryController {

    private final CategoryRepository categoryRepository;

    /**
     * Obtiene la lista completa de categorías disponibles en la tienda.
     * Este método es público y se utiliza para llenar los desplegables del frontend.
     * 
     * @return Lista de objetos Category
     */
    @GetMapping
    @Operation(summary = "Obtener todas las categorías")
    public ResponseEntity<List<Category>> getAllCategories() {
        return ResponseEntity.ok(categoryRepository.findAll());
    }

    @PostMapping
    @Operation(summary = "Crear una nueva categoría (Admin)")
    public ResponseEntity<Category> createCategory(@RequestBody Category category) {
        log.info("Creando nueva categoría: {}", category.getName());
        return ResponseEntity.ok(categoryRepository.save(category));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar una categoría (Admin)")
    public ResponseEntity<Category> updateCategory(@PathVariable Long id, @RequestBody Category categoryDetails) {
        log.info("Actualizando categoría ID {}: {}", id, categoryDetails.getName());
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada"));
        
        category.setName(categoryDetails.getName());
        return ResponseEntity.ok(categoryRepository.save(category));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar una categoría (Admin)")
    public ResponseEntity<?> deleteCategory(@PathVariable Long id) {
        log.warn("Eliminando categoría ID: {}", id);
        if (!categoryRepository.existsById(id)) {
            throw new RuntimeException("Categoría no encontrada");
        }
        categoryRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }
}
