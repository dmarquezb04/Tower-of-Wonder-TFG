package com.tow.backend.admin.controller;

import com.tow.backend.common.dto.ApiResponse;
import com.tow.backend.exception.NotFoundException;
import com.tow.backend.shop.entity.Product;
import com.tow.backend.shop.repository.ProductRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para la gestión administrativa del catálogo de productos.
 *
 * <p>Permite crear, actualizar y eliminar productos de la tienda.
 * Todos los endpoints requieren el rol ADMIN.
 * Los errores de negocio son gestionados por {@link com.tow.backend.exception.GlobalExceptionHandler}.
 *
 * @author Darío Márquez Bautista
 */
@RestController
@RequestMapping("/admin/products")
@RequiredArgsConstructor
@Tag(name = "Administración - Tienda", description = "CRUD de productos (solo ADMIN)")
public class AdminShopController {

    private final ProductRepository productRepository;

    /**
     * Devuelve la lista completa de productos, incluyendo los inactivos.
     *
     * @return 200 OK con lista de productos
     */
    @GetMapping
    @Operation(summary = "Listar todos los productos (incluye inactivos)")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Lista devuelta correctamente"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Acceso denegado — se requiere rol ADMIN")
    })
    public ResponseEntity<List<Product>> getAllProducts() {
        return ResponseEntity.ok(productRepository.findAll());
    }

    /**
     * Crea un nuevo producto en el catálogo.
     *
     * @param product datos del nuevo producto
     * @return 200 OK con el producto creado
     */
    @PostMapping
    @Operation(summary = "Crear un nuevo producto")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Producto creado correctamente"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Acceso denegado — se requiere rol ADMIN")
    })
    public ResponseEntity<Product> createProduct(@RequestBody Product product) {
        return ResponseEntity.ok(productRepository.save(product));
    }

    /**
     * Actualiza los datos de un producto existente.
     *
     * @param id             ID del producto a actualizar
     * @param productDetails nuevos datos del producto
     * @return 200 OK con el producto actualizado
     */
    @PutMapping("/{id}")
    @Operation(summary = "Actualizar un producto existente")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Producto actualizado correctamente"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Acceso denegado — se requiere rol ADMIN"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Producto no encontrado")
    })
    public ResponseEntity<Product> updateProduct(@PathVariable Long id, @RequestBody Product productDetails) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Producto no encontrado con ID: " + id));

        product.setName(productDetails.getName());
        product.setDescription(productDetails.getDescription());
        product.setPrice(productDetails.getPrice());
        product.setStock(productDetails.getStock());
        product.setImageUrl(productDetails.getImageUrl());
        product.setCategory(productDetails.getCategory());
        product.setActive(productDetails.getActive());

        return ResponseEntity.ok(productRepository.save(product));
    }

    /**
     * Elimina un producto del catálogo.
     *
     * @param id ID del producto a eliminar
     * @return 200 OK con mensaje de confirmación
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar un producto")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Producto eliminado correctamente"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Acceso denegado — se requiere rol ADMIN")
    })
    public ResponseEntity<ApiResponse> deleteProduct(@PathVariable Long id) {
        productRepository.deleteById(id);
        return ResponseEntity.ok(new ApiResponse("Producto eliminado correctamente"));
    }
}

