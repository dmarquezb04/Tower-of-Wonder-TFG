package com.tow.backend.admin.controller;

import com.tow.backend.common.dto.ApiResponse;
import com.tow.backend.exception.NotFoundException;
import com.tow.backend.shop.entity.Product;
import com.tow.backend.shop.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminShopControllerTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private AdminShopController adminShopController;

    private Product product;

    @BeforeEach
    void setUp() {
        product = new Product();
        product.setId(1L);
        product.setName("Admin Product");
        product.setPrice(new BigDecimal("15.00"));
        product.setStock(20);
        product.setActive(true);
    }

    @Test
    void getAllProducts_ReturnsList() {
        when(productRepository.findAll()).thenReturn(Arrays.asList(product));

        ResponseEntity<List<Product>> response = adminShopController.getAllProducts();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        assertEquals("Admin Product", response.getBody().get(0).getName());
    }

    @Test
    void createProduct_Success() {
        when(productRepository.save(any(Product.class))).thenReturn(product);

        ResponseEntity<Product> response = adminShopController.createProduct(product);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Admin Product", response.getBody().getName());
        verify(productRepository).save(product);
    }

    @Test
    void updateProduct_Success() {
        Product updatedDetails = new Product();
        updatedDetails.setName("Updated Product");
        updatedDetails.setPrice(new BigDecimal("20.00"));
        updatedDetails.setStock(30);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenReturn(product);

        ResponseEntity<Product> response = adminShopController.updateProduct(1L, updatedDetails);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Updated Product", product.getName());
        assertEquals(new BigDecimal("20.00"), product.getPrice());
        assertEquals(30, product.getStock());
        verify(productRepository).save(product);
    }

    @Test
    void updateProduct_NotFound_ThrowsException() {
        Product updatedDetails = new Product();
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> 
            adminShopController.updateProduct(99L, updatedDetails)
        );
        verify(productRepository, never()).save(any());
    }

    @Test
    void deleteProduct_Success() {
        ResponseEntity<ApiResponse> response = adminShopController.deleteProduct(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Producto eliminado correctamente", response.getBody().getMessage());
        verify(productRepository).deleteById(1L);
    }
}
