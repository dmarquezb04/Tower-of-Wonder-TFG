package com.tow.backend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Test de arranque del contexto Spring Boot.
 *
 * <p>Verifica que el contexto de la aplicaciÃ³n se carga correctamente:
 * todas las configuraciones son vÃ¡lidas, los beans se instancian
 * sin errores y la aplicaciÃ³n es capaz de arrancar.
 *
 * <p>Es el test mÃ¡s bÃ¡sico pero tambiÃ©n el mÃ¡s importante: si este
 * falla, ningÃºn otro test tiene sentido.
 *
 * @author DarÃ­o MÃ¡rquez Bautista
 */
@SpringBootTest
@ActiveProfiles("test")
class TowerOfWonderApplicationTests {

    /**
     * Verifica que el contexto Spring se carga sin errores.
     * No necesita assertions â€” si el contexto falla al cargar,
     * el test falla automÃ¡ticamente.
     */
    @Test
    void contextLoads() {
        // Test implÃ­cito: si el contexto Spring arranca sin excepciones, pasa.
    }
}

