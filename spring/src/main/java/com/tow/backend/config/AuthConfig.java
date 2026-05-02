package com.tow.backend.config;

import com.warrenstrange.googleauth.GoogleAuthenticator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Beans de configuración del módulo de autenticación.
 *
 * <p>Centraliza la creación de beans que no pertenecen a ningún
 * módulo concreto o que son compartidos por varios módulos.
 *
 * @author Darío Márquez Bautista
 */
@Configuration
public class AuthConfig {

    /**
     * Bean de Google Authenticator para verificar códigos TOTP.
     *
     * <p>La librería {@code com.warrenstrange:googleauth} implementa RFC 6238
     * y es compatible con la librería PHP {@code PHPGangsta_GoogleAuthenticator}.
     * Los secretos Base32 almacenados en la BD son 100% reutilizables.
     *
     * @return instancia de GoogleAuthenticator con ventana de tolerancia ±30s
     */
    @Bean
    public GoogleAuthenticator googleAuthenticator() {
        return new GoogleAuthenticator();
    }
}
