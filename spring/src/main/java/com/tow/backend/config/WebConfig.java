package com.tow.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import org.springframework.lang.NonNull;

/**
 * Configuración Web para servir recursos adicionales como el Javadoc.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(@NonNull ResourceHandlerRegistry registry) {
        // Servimos el contenido de esa carpeta en la URL /docs/javadoc/**
        registry.addResourceHandler("/docs/javadoc/**")
                .addResourceLocations("file:target/reports/apidocs/");

        // También permitimos servir el frontend si estuviera en static (opcional)
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/");
    }
}
