package com.tow.backend.shop.controller;

import com.tow.backend.common.dto.ApiResponse;
import com.tow.backend.email.service.MailService;
import com.tow.backend.exception.BadRequestException;
import com.tow.backend.exception.NotFoundException;
import com.tow.backend.exception.UnauthorizedException;
import com.tow.backend.shop.entity.Order;
import com.tow.backend.shop.entity.OrderItem;
import com.tow.backend.shop.entity.Product;
import com.tow.backend.shop.repository.OrderRepository;
import com.tow.backend.shop.repository.ProductRepository;
import com.tow.backend.user.entity.User;
import com.tow.backend.user.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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

/**
 * Controlador REST para la tienda: catÃ¡logo de productos y checkout.
 *
 * <p>El endpoint de productos ({@code GET /shop/products}) es pÃºblico.
 * El endpoint de checkout ({@code POST /shop/checkout}) requiere autenticaciÃ³n JWT.
 * Los errores de negocio son gestionados por {@link com.tow.backend.exception.GlobalExceptionHandler}.
 *
 * @author DarÃ­o MÃ¡rquez Bautista
 */
@RestController
@RequestMapping("/shop")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Tienda", description = "CatÃ¡logo de productos y checkout simulado")
public class ShopController {

    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final MailService mailService;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    /**
     * Devuelve el catÃ¡logo de productos activos, con filtrado opcional por categorÃ­a.
     *
     * @param category nombre de la categorÃ­a para filtrar (opcional)
     * @return 200 OK con lista de productos
     */
    @GetMapping("/products")
    @Operation(summary = "Obtener catÃ¡logo de productos activos")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "CatÃ¡logo devuelto correctamente")
    })
    public ResponseEntity<List<Product>> getProducts(@RequestParam(required = false) String category) {
        if (category != null && !category.isEmpty()) {
            return ResponseEntity.ok(productRepository.findByCategoryNameAndActiveTrue(category));
        }
        return ResponseEntity.ok(productRepository.findByActiveTrue());
    }

    /**
     * Simula el proceso de compra del carrito del usuario autenticado.
     *
     * <p>Proceso:
     * <ol>
     *   <li>Valida que el usuario estÃ© autenticado</li>
     *   <li>Verifica que el carrito no estÃ© vacÃ­o</li>
     *   <li>Comprueba el stock de cada producto</li>
     *   <li>Crea el pedido y reduce el stock</li>
     *   <li>EnvÃ­a un recibo por email (fallo no crÃ­tico)</li>
     * </ol>
     *
     * @param cartItems lista de productos con su cantidad ({@code id} y {@code quantity})
     * @return 200 OK con el ID del pedido y el total
     */
    @PostMapping("/checkout")
    @Operation(summary = "Simular compra del carrito (requiere autenticaciÃ³n)")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Compra realizada correctamente"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Carrito vacÃ­o o stock insuficiente"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "No autenticado"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Producto no encontrado")
    })
    public ResponseEntity<Map<String, Object>> checkout(@RequestBody List<Map<String, Object>> cartItems) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new UnauthorizedException("Debes iniciar sesiÃ³n para realizar una compra");
        }

        if (cartItems == null || cartItems.isEmpty()) {
            throw new BadRequestException("El carrito estÃ¡ vacÃ­o");
        }

        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));

        Order order = new Order();
        order.setUser(user);
        BigDecimal total = BigDecimal.ZERO;

        for (Map<String, Object> itemData : cartItems) {
            Long productId = Long.valueOf(itemData.get("id").toString());
            Integer quantity = Integer.valueOf(itemData.get("quantity").toString());

            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new NotFoundException("Producto no encontrado con ID: " + productId));

            if (product.getStock() < quantity) {
                throw new BadRequestException("Stock insuficiente para el producto: " + product.getName());
            }

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
        log.info("Compra realizada. Pedido ID: {}, Usuario: {}, Total: {}", savedOrder.getId(), email, total);

        // Enviar recibo por email â€” el fallo es no crÃ­tico (no interrumpe la respuesta)
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
            emailVars.put("baseUrl", frontendUrl);

            mailService.sendHtmlEmail(user.getEmail(), "Recibo de Compra - Tower of Wonder", "order_receipt", emailVars);
        } catch (Exception e) {
            log.error("Error al enviar recibo del pedido {}: {}", savedOrder.getId(), e.getMessage());
        }

        return ResponseEntity.ok(Map.of(
                "message", "Compra simulada con Ã©xito. Se ha enviado un recibo a tu correo.",
                "orderId", savedOrder.getId(),
                "total", total
        ));
    }
}

