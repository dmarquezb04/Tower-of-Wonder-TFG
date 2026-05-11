package com.tow.backend.config;

import com.tow.backend.security.JwtAuthenticationFilter;
import com.tow.backend.security.UserDetailsServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.config.Customizer;

import java.util.Arrays;
import java.util.List;

/**
 * Configuración de seguridad Spring Security.
 *
 * @author Darío Márquez Bautista
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final UserDetailsServiceImpl userDetailsService;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(Customizer.withDefaults())
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .authenticationProvider(daoAuthProvider())
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
            .authorizeHttpRequests(auth -> auth
                // 1. ACCESO RESTRINGIDO (ADMIN)
                .requestMatchers("/admin/**", "/metrics/admin/**", "/characters/admin/**", "/news/admin/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/characters", "/news", "/categories").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/characters/**", "/news/**", "/categories/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/characters/**", "/news/**", "/categories/**").hasRole("ADMIN")

                // 2. ACCESO PÚBLICO
                .requestMatchers("/health", "/error", "/v3/api-docs/**", "/api-docs/**", "/swagger-ui/**", "/swagger-ui.html", "/docs/javadoc/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/auth/login", "/auth/register", "/auth/verify-2fa", "/auth/reactivate", "/contacto", "/metrics/track").permitAll()
                .requestMatchers(HttpMethod.GET, "/shop/products", "/categories/**", "/characters/**", "/news/**").permitAll()
                .requestMatchers("/newsletter/**").permitAll()
                
                // 3. RESTO DE PETICIONES (Requiere login)
                .anyRequest().authenticated()
            );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config
    ) throws Exception {
        return config.getAuthenticationManager();
    }

    private DaoAuthenticationProvider daoAuthProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    /**
     * Configuración de CORS basada en la URL del frontend definida en application.yml.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Permitimos el origen del frontend (producción y local)
        configuration.setAllowedOrigins(List.of(frontendUrl, "http://localhost:8080", "http://localhost:5173"));
        
        // Métodos permitidos
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        
        // Cabeceras permitidas
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Cache-Control", "Content-Type", "X-Requested-With"));
        
        // Permitir envío de cookies/auth headers
        configuration.setAllowCredentials(true);
        
        // Exponer cabeceras si fuera necesario (ej: para paginación)
        configuration.setExposedHeaders(List.of("Authorization"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}

