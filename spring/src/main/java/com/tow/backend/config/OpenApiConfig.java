package com.tow.backend.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

/**
 * ConfiguraciÃ³n de la documentaciÃ³n OpenAPI 3 (Swagger UI).
 *
 * <p>Define los metadatos generales de la API y el esquema de seguridad JWT,
 * de modo que el botÃ³n "Authorize" en Swagger UI permita enviar el token Bearer
 * en las peticiones de prueba a endpoints protegidos.
 *
 * <p>La UI es accesible en: {@code http://localhost:8080/swagger-ui.html}
 *
 * @author DarÃ­o MÃ¡rquez Bautista
 */
@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Tower of Wonder â€” API REST",
                version = "1.0.0",
                description = "DocumentaciÃ³n completa del backend de Tower of Wonder (TFG). " +
                        "Los endpoints marcados con el candado requieren autenticaciÃ³n JWT.",
                contact = @Contact(
                        name = "DarÃ­o MÃ¡rquez Bautista",
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
    // La configuraciÃ³n se realiza mediante anotaciones a nivel de clase.
    // SpringDoc OpenAPI las procesa automÃ¡ticamente al arrancar la aplicaciÃ³n.
}

