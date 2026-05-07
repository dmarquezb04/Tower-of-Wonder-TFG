package com.tow.backend.config;

import com.warrenstrange.googleauth.GoogleAuthenticator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Beans de configuraciÃ³n del mÃ³dulo de autenticaciÃ³n.
 *
 * <p>Centraliza la creaciÃ³n de beans que no pertenecen a ningÃºn
 * mÃ³dulo concreto o que son compartidos por varios mÃ³dulos.
 *
 * @author DarÃ­o MÃ¡rquez Bautista
 */
@Configuration
public class AuthConfig {

    /**
     * Bean de Google Authenticator para verificar cÃ³digos TOTP.
     *
     * <p>La librerÃ­a {@code com.warrenstrange:googleauth} implementa RFC 6238
     * y es compatible con la librerÃ­a PHP {@code PHPGangsta_GoogleAuthenticator}.
     * Los secretos Base32 almacenados en la BD son 100% reutilizables.
     *
     * @return instancia de GoogleAuthenticator con ventana de tolerancia Â±30s
     */
    @Bean
    public GoogleAuthenticator googleAuthenticator() {
        return new GoogleAuthenticator();
    }
}

