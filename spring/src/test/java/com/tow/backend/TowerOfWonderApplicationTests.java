package com.tow.backend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Test de arranque del contexto Spring Boot.
 *
 * <p>Verifica que el contexto de la aplicación se carga correctamente:
 * todas las configuraciones son válidas, los beans se instancian
 * sin errores y la aplicación es capaz de arrancar.
 *
 * <p>Es el test más básico pero también el más importante: si este
 * falla, ningún otro test tiene sentido.
 *
 * @author Darío Márquez Bautista
 */
@SpringBootTest
@ActiveProfiles("test")
class TowerOfWonderApplicationTests {

    /**
     * Verifica que el contexto Spring se carga sin errores.
     * No necesita assertions — si el contexto falla al cargar,
     * el test falla automáticamente.
     */
    @Test
    void contextLoads() {
        // Test implícito: si el contexto Spring arranca sin excepciones, pasa.
    }
}
