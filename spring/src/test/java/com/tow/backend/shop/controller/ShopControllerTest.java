package com.tow.backend.shop.controller;

import com.tow.backend.email.service.MailService;
import com.tow.backend.exception.BadRequestException;
import com.tow.backend.exception.NotFoundException;
import com.tow.backend.exception.UnauthorizedException;
import com.tow.backend.shop.entity.Order;
import com.tow.backend.shop.entity.Product;
import com.tow.backend.shop.repository.OrderRepository;
import com.tow.backend.shop.repository.ProductRepository;
import com.tow.backend.user.entity.User;
import com.tow.backend.user.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ShopControllerTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private MailService mailService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private ShopController shopController;

    private Product product;
    private User user;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(shopController, "frontendUrl", "http://localhost:3000");

        product = new Product();
        product.setId(1L);
        product.setName("Test Product");
        product.setPrice(new BigDecimal("10.50"));
        product.setStock(10);
        product.setActive(true);

        user = new User();
        user.setEmail("test@test.com");
        user.setUsername("testuser");
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getProducts_NoCategory_ReturnsAllActive() {
        when(productRepository.findByActiveTrue()).thenReturn(Arrays.asList(product));

        ResponseEntity<List<Product>> response = shopController.getProducts(null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        assertEquals("Test Product", response.getBody().get(0).getName());
    }

    @Test
    void getProducts_WithCategory_ReturnsFiltered() {
        when(productRepository.findByCategoryNameAndActiveTrue("Merch")).thenReturn(Arrays.asList(product));

        ResponseEntity<List<Product>> response = shopController.getProducts("Merch");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void checkout_Success() {
        // Mock Security Context
        SecurityContextHolder.getContext().setAuthentication(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn("test@test.com");
        when(authentication.getName()).thenReturn("test@test.com");

        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        
        Order savedOrder = new Order();
        savedOrder.setId(100L);
        savedOrder.setOrderDate(LocalDateTime.now());
        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);

        List<Map<String, Object>> cartItems = new ArrayList<>();
        Map<String, Object> item = new HashMap<>();
        item.put("id", 1L);
        item.put("quantity", 2);
        cartItems.add(item);

        ResponseEntity<Map<String, Object>> response = shopController.checkout(cartItems);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody().get("orderId"));
        assertEquals(new BigDecimal("21.00"), response.getBody().get("total"));

        // Verify stock reduced
        assertEquals(8, product.getStock());
        verify(productRepository).save(product);
        
        // Verify email sent
        verify(mailService).sendHtmlEmail(eq("test@test.com"), anyString(), eq("order_receipt"), anyMap());
    }

    @Test
    void checkout_NotAuthenticated_ThrowsException() {
        SecurityContextHolder.getContext().setAuthentication(null);

        List<Map<String, Object>> cartItems = new ArrayList<>();

        assertThrows(UnauthorizedException.class, () -> shopController.checkout(cartItems));
    }

    @Test
    void checkout_EmptyCart_ThrowsException() {
        SecurityContextHolder.getContext().setAuthentication(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn("test@test.com");

        List<Map<String, Object>> cartItems = new ArrayList<>();

        assertThrows(BadRequestException.class, () -> shopController.checkout(cartItems));
    }

    @Test
    void checkout_InsufficientStock_ThrowsException() {
        SecurityContextHolder.getContext().setAuthentication(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn("test@test.com");
        when(authentication.getName()).thenReturn("test@test.com");

        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        List<Map<String, Object>> cartItems = new ArrayList<>();
        Map<String, Object> item = new HashMap<>();
        item.put("id", 1L);
        item.put("quantity", 20); // More than stock (10)
        cartItems.add(item);

        assertThrows(BadRequestException.class, () -> shopController.checkout(cartItems));
    }
}
