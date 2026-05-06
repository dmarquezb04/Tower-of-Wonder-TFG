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

/**
 * Configuración de seguridad Spring Security — Fase 1 (JWT Auth).
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

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .authenticationProvider(daoAuthProvider())
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
            .authorizeHttpRequests(auth -> auth
                // 1. ACCESO PÚBLICO
                .requestMatchers("/health", "/error", "/v3/api-docs/**", "/api-docs/**", "/swagger-ui/**", "/swagger-ui.html", "/docs/javadoc/**").permitAll()
                
                // Autenticación y Registro
                .requestMatchers(HttpMethod.POST, "/auth/login", "/auth/register", "/auth/verify-2fa", "/auth/reactivate").permitAll()
                
                // Lectura pública (Tienda, Categorías, Personajes, Noticias)
                .requestMatchers(HttpMethod.GET, "/shop/products", "/categories/**",
                        "/characters", "/characters/*",
                        "/news", "/news/*").permitAll()
                
                // Otros servicios públicos
                .requestMatchers("/newsletter/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/metrics/track").permitAll()
                
                // 2. ACCESO RESTRINGIDO (ADMIN)
                .requestMatchers("/admin/**", "/metrics/admin/**", "/categories/**",
                        "/characters/admin/**", "/news/admin/**").hasRole("ADMIN")
                // POST/PUT/DELETE en /characters y /news requieren rol ADMIN
                .requestMatchers(HttpMethod.POST, "/characters", "/news").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/characters/**", "/news/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/characters/**", "/news/**").hasRole("ADMIN")
                
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
}
