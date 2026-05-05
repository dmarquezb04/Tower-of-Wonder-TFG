package com.tow.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;
import java.nio.file.Paths;

/**
 * Configuración Web para servir recursos adicionales como el Javadoc.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Obtenemos la ruta absoluta de la carpeta donde Maven genera el Javadoc
        String javadocPath = Paths.get("target/reports/apidocs").toAbsolutePath().toUri().toString();
        
        // Servimos el contenido de esa carpeta en la URL /docs/javadoc/**
        registry.addResourceHandler("/docs/javadoc/**")
                .addResourceLocations(javadocPath);
                
        // También permitimos servir el frontend si estuviera en static (opcional)
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/");
    }
}
