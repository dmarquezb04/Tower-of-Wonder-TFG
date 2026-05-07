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
 * Controlador REST para la gestiÃ³n de categorÃ­as de productos.
 *
 * <p>Los endpoints de lectura son pÃºblicos. Las operaciones de escritura
 * (crear, actualizar, eliminar) requieren el rol ADMIN.
 * Los errores de negocio son gestionados por {@link com.tow.backend.exception.GlobalExceptionHandler}.
 *
 * @author DarÃ­o MÃ¡rquez Bautista
 */
@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "CategorÃ­as", description = "GestiÃ³n de categorÃ­as de productos")
public class CategoryController {

    private final CategoryRepository categoryRepository;

    /**
     * Devuelve la lista completa de categorÃ­as disponibles.
     *
     * <p>Endpoint pÃºblico, utilizado para rellenar los filtros del frontend.
     *
     * @return 200 OK con lista de categorÃ­as
     */
    @GetMapping
    @Operation(summary = "Obtener todas las categorÃ­as")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Lista de categorÃ­as devuelta correctamente")
    public ResponseEntity<List<Category>> getAllCategories() {
        return ResponseEntity.ok(categoryRepository.findAll());
    }

    /**
     * Crea una nueva categorÃ­a en el sistema.
     *
     * @param category datos de la nueva categorÃ­a
     * @return 200 OK con la categorÃ­a creada
     */
    @PostMapping
    @Operation(summary = "Crear una nueva categorÃ­a (Admin)")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "CategorÃ­a creada correctamente"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Acceso denegado â€” se requiere rol ADMIN")
    })
    public ResponseEntity<Category> createCategory(@RequestBody Category category) {
        log.info("Creando nueva categorÃ­a: {}", category.getName());
        return ResponseEntity.ok(categoryRepository.save(category));
    }

    /**
     * Actualiza el nombre de una categorÃ­a existente.
     *
     * @param id              ID de la categorÃ­a a actualizar
     * @param categoryDetails datos actualizados de la categorÃ­a
     * @return 200 OK con la categorÃ­a actualizada
     */
    @PutMapping("/{id}")
    @Operation(summary = "Actualizar una categorÃ­a (Admin)")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "CategorÃ­a actualizada correctamente"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Acceso denegado â€” se requiere rol ADMIN"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "CategorÃ­a no encontrada")
    })
    public ResponseEntity<Category> updateCategory(@PathVariable Long id, @RequestBody Category categoryDetails) {
        log.info("Actualizando categorÃ­a ID {}: {}", id, categoryDetails.getName());
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("CategorÃ­a no encontrada con ID: " + id));

        category.setName(categoryDetails.getName());
        return ResponseEntity.ok(categoryRepository.save(category));
    }

    /**
     * Elimina una categorÃ­a del sistema.
     *
     * @param id ID de la categorÃ­a a eliminar
     * @return 200 OK con mensaje de confirmaciÃ³n
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar una categorÃ­a (Admin)")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "CategorÃ­a eliminada correctamente"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Acceso denegado â€” se requiere rol ADMIN"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "CategorÃ­a no encontrada")
    })
    public ResponseEntity<ApiResponse> deleteCategory(@PathVariable Long id) {
        log.warn("Eliminando categorÃ­a ID: {}", id);
        if (!categoryRepository.existsById(id)) {
            throw new NotFoundException("CategorÃ­a no encontrada con ID: " + id);
        }
        categoryRepository.deleteById(id);
        return ResponseEntity.ok(new ApiResponse("CategorÃ­a eliminada correctamente"));
    }
}

