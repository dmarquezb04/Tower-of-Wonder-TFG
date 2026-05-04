package com.tow.backend.config;

import com.tow.backend.shop.entity.Product;
import com.tow.backend.shop.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final ProductRepository productRepository;

    @Override
    public void run(String... args) throws Exception {
        if (productRepository.count() == 0) {
            log.info("Inicializando datos de prueba para la Tienda...");

            List<Product> products = List.of(
                Product.builder()
                    .name("Bendición Lunar")
                    .description("Suscripción de 30 días")
                    .price(new BigDecimal("4.99"))
                    .stock(999)
                    .category("Bendición Lunar")
                    .active(true)
                    .build(),
                Product.builder()
                    .name("PB del Gnóstico")
                    .description("Desbloquea el Pase de Batalla premium")
                    .price(new BigDecimal("9.99"))
                    .stock(999)
                    .category("Pase de Batalla")
                    .active(true)
                    .build(),
                Product.builder()
                    .name("Notas del Viajero")
                    .description("Niveles adicionales del PB")
                    .price(new BigDecimal("11.99"))
                    .stock(999)
                    .category("Pase de Batalla")
                    .active(true)
                    .build(),
                Product.builder()
                    .name("PB Himno de la Perla")
                    .description("El mejor pase de batalla")
                    .price(new BigDecimal("19.99"))
                    .stock(999)
                    .category("Pase de Batalla")
                    .active(true)
                    .build(),
                Product.builder()
                    .name("Cristal génesis ×60")
                    .description("¡Doble! +60")
                    .price(new BigDecimal("0.99"))
                    .stock(999)
                    .category("Cristal génesis")
                    .active(true)
                    .build(),
                Product.builder()
                    .name("Cristal génesis ×300")
                    .description("¡Doble! +300")
                    .price(new BigDecimal("4.99"))
                    .stock(999)
                    .category("Cristal génesis")
                    .active(true)
                    .build(),
                Product.builder()
                    .name("Cristal génesis ×980")
                    .description("¡Doble! +980")
                    .price(new BigDecimal("14.99"))
                    .stock(999)
                    .category("Cristal génesis")
                    .active(true)
                    .build(),
                Product.builder()
                    .name("Cristal génesis ×1980")
                    .description("¡Doble! +1980")
                    .price(new BigDecimal("29.99"))
                    .stock(999)
                    .category("Cristal génesis")
                    .active(true)
                    .build(),
                Product.builder()
                    .name("Cristal génesis ×3280")
                    .description("¡Doble! +3280")
                    .price(new BigDecimal("49.99"))
                    .stock(999)
                    .category("Cristal génesis")
                    .active(true)
                    .build(),
                Product.builder()
                    .name("Cristal génesis ×6480")
                    .description("¡Doble! +6480")
                    .price(new BigDecimal("99.99"))
                    .stock(999)
                    .category("Cristal génesis")
                    .active(true)
                    .build()
            );

            productRepository.saveAll(products);
            log.info("Se han guardado {} productos de prueba.", products.size());
        }
    }
}
