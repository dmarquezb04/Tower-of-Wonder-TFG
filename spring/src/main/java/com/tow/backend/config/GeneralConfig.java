package com.tow.backend.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.client.RestTemplate;
import org.springframework.boot.web.client.RestTemplateBuilder;
import java.time.Duration;

/**
 * Configuración general del sistema.
 * 
 * Habilita:
 * - @EnableAsync: Para ejecutar tareas en segundo plano (ej: track de métricas).
 * - @EnableCaching: Para almacenar en memoria resultados repetitivos (ej: GeoIP).
 */
@Configuration
@EnableAsync
@EnableCaching
public class GeneralConfig {

    /**
     * Define el bean de RestTemplate para peticiones HTTP externas.
     * Configura timeouts para evitar que el hilo se bloquee indefinidamente.
     */
    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder
                .setConnectTimeout(Duration.ofSeconds(3))
                .setReadTimeout(Duration.ofSeconds(3))
                .build();
    }
}
