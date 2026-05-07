package com.tow.backend.shop.controller;

import com.tow.backend.common.dto.ApiResponse;
import com.tow.backend.exception.NotFoundException;
import com.tow.backend.shop.entity.Category;
import com.tow.backend.shop.repository.CategoryRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para la gestión de categorías de productos.
 *
 * <p>Los endpoints de lectura son públicos. Las operaciones de escritura
 * (crear, actualizar, eliminar) requieren el rol ADMIN.
 * Los errores de negocio son gestionados por {@link com.tow.backend.exception.GlobalExceptionHandler}.
 *
 * @author Darío Márquez Bautista
 */
@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Categorías", description = "Gestión de categorías de productos")
public class CategoryController {

    private final CategoryRepository categoryRepository;

    /**
     * Devuelve la lista completa de categorías disponibles.
     *
     * <p>Endpoint público, utilizado para rellenar los filtros del frontend.
     *
     * @return 200 OK con lista de categorías
     */
    @GetMapping
    @Operation(summary = "Obtener todas las categorías")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Lista de categorías devuelta correctamente")
    public ResponseEntity<List<Category>> getAllCategories() {
        return ResponseEntity.ok(categoryRepository.findAllByOrderByIdAsc());
    }

    /**
     * Crea una nueva categoría en el sistema.
     *
     * @param category datos de la nueva categoría
     * @return 200 OK con la categoría creada
     */
    @PostMapping
    @Operation(summary = "Crear una nueva categoría (Admin)")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Categoría creada correctamente"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Acceso denegado — se requiere rol ADMIN")
    })
    public ResponseEntity<Category> createCategory(@RequestBody Category category) {
        log.info("Creando nueva categoría: {}", category.getName());
        return ResponseEntity.ok(categoryRepository.save(category));
    }

    /**
     * Actualiza el nombre de una categoría existente.
     *
     * @param id              ID de la categoría a actualizar
     * @param categoryDetails datos actualizados de la categoría
     * @return 200 OK con la categoría actualizada
     */
    @PutMapping("/{id}")
    @Operation(summary = "Actualizar una categoría (Admin)")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Categoría actualizada correctamente"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Acceso denegado — se requiere rol ADMIN"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Categoría no encontrada")
    })
    public ResponseEntity<Category> updateCategory(@PathVariable Long id, @RequestBody Category categoryDetails) {
        log.info("Actualizando categoría ID {}: {}", id, categoryDetails.getName());
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Categoría no encontrada con ID: " + id));

        category.setName(categoryDetails.getName());
        return ResponseEntity.ok(categoryRepository.save(category));
    }

    /**
     * Elimina una categoría del sistema.
     *
     * @param id ID de la categoría a eliminar
     * @return 200 OK con mensaje de confirmación
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar una categoría (Admin)")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Categoría eliminada correctamente"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Acceso denegado — se requiere rol ADMIN"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Categoría no encontrada")
    })
    public ResponseEntity<ApiResponse> deleteCategory(@PathVariable Long id) {
        log.warn("Eliminando categoría ID: {}", id);
        if (!categoryRepository.existsById(id)) {
            throw new NotFoundException("Categoría no encontrada con ID: " + id);
        }
        categoryRepository.deleteById(id);
        return ResponseEntity.ok(new ApiResponse("Categoría eliminada correctamente"));
    }
}

