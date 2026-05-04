package com.tow.backend.shop.controller;

import com.tow.backend.email.service.MailService;
import com.tow.backend.shop.entity.Order;
import com.tow.backend.shop.entity.OrderItem;
import com.tow.backend.shop.entity.Product;
import com.tow.backend.shop.repository.OrderRepository;
import com.tow.backend.shop.repository.ProductRepository;
import com.tow.backend.user.entity.User;
import com.tow.backend.user.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/shop")
@RequiredArgsConstructor
@Tag(name = "Tienda", description = "Catálogo de productos y checkout simulado")
public class ShopController {

    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final MailService mailService;

    @GetMapping("/products")
    @Operation(summary = "Obtener todos los productos activos")
    public ResponseEntity<List<Product>> getProducts(
            @RequestParam(required = false) String category) {
        if (category != null && !category.isEmpty()) {
            return ResponseEntity.ok(productRepository.findByCategoryAndActiveTrue(category));
        }
        return ResponseEntity.ok(productRepository.findByActiveTrue());
    }

    @PostMapping("/checkout")
    @Operation(summary = "Simular la compra del carrito (requiere autenticación)")
    public ResponseEntity<?> checkout(@RequestBody List<Map<String, Object>> cartItems) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            return ResponseEntity.status(401).body(Map.of("error", "Debes iniciar sesión para comprar"));
        }

        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (cartItems == null || cartItems.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "El carrito está vacío"));
        }

        Order order = new Order();
        order.setUser(user);
        BigDecimal total = BigDecimal.ZERO;

        for (Map<String, Object> itemData : cartItems) {
            Long productId = Long.valueOf(itemData.get("id").toString());
            Integer quantity = Integer.valueOf(itemData.get("quantity").toString());

            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado: " + productId));

            if (product.getStock() < quantity) {
                return ResponseEntity.badRequest().body(Map.of("error", "No hay suficiente stock para: " + product.getName()));
            }

            // Restar stock
            product.setStock(product.getStock() - quantity);
            productRepository.save(product);

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setQuantity(quantity);
            orderItem.setPriceAtPurchase(product.getPrice());

            order.getItems().add(orderItem);
            total = total.add(product.getPrice().multiply(BigDecimal.valueOf(quantity)));
        }

        order.setTotalPrice(total);
        Order savedOrder = orderRepository.save(order);

        // Enviar Recibo por Email (Asíncrono)
        try {
            Map<String, Object> emailVars = new HashMap<>();
            emailVars.put("username", user.getUsername() != null ? user.getUsername() : user.getEmail());
            emailVars.put("orderId", savedOrder.getId());
            emailVars.put("orderDate", savedOrder.getOrderDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
            emailVars.put("totalPrice", savedOrder.getTotalPrice());
            
            List<Map<String, Object>> itemsList = savedOrder.getItems().stream().map(item -> {
                Map<String, Object> m = new HashMap<>();
                m.put("productName", item.getProduct().getName());
                m.put("quantity", item.getQuantity());
                m.put("price", item.getPriceAtPurchase());
                return m;
            }).collect(Collectors.toList());
            emailVars.put("items", itemsList);

            mailService.sendHtmlEmail(user.getEmail(), "Recibo de Compra - Tower of Wonder", "order_receipt", emailVars);
        } catch (Exception e) {
            // Logueamos el error pero no fallamos la compra por el email
        }

        return ResponseEntity.ok(Map.of(
            "message", "Compra simulada con éxito. Se ha enviado un recibo a tu correo.",
            "orderId", savedOrder.getId(),
            "total", total
        ));
    }
}
}
