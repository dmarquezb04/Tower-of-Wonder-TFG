package com.tow.backend.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración de la documentación OpenAPI 3 (Swagger UI).
 *
 * <p>Define los metadatos generales de la API y el esquema de seguridad JWT,
 * de modo que el botón "Authorize" en Swagger UI permita enviar el token Bearer
 * en las peticiones de prueba a endpoints protegidos.
 *
 * <p>La UI es accesible en: {@code http://localhost:8080/swagger-ui.html}
 *
 * @author Darío Márquez Bautista
 */
@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Tower of Wonder — API REST",
                version = "1.0.0",
                description = "Documentación completa del backend de Tower of Wonder (TFG). " +
                        "Los endpoints marcados con el candado requieren autenticación JWT.",
                contact = @Contact(
                        name = "Darío Márquez Bautista",
                        email = "dario@towerofwonder.com"
                )
        ),
        servers = {
                @Server(url = "/", description = "Servidor local")
        }
)
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        description = "Introduce el token JWT obtenido en /auth/login (sin el prefijo 'Bearer ')"
)
public class OpenApiConfig {
    // La configuración se realiza mediante anotaciones a nivel de clase.
    // SpringDoc OpenAPI las procesa automáticamente al arrancar la aplicación.
}

