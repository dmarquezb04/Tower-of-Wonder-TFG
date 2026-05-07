package com.tow.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Punto de entrada de la aplicaciÃ³n Spring Boot â€” Tower of Wonder Backend.
 *
 * <p>{@code @SpringBootApplication} es una meta-anotaciÃ³n que combina:
 * <ul>
 *   <li>{@code @Configuration} â€” marca esta clase como fuente de beans Spring</li>
 *   <li>{@code @EnableAutoConfiguration} â€” activa la autoconfiguraciÃ³n de Spring Boot</li>
 *   <li>{@code @ComponentScan} â€” escanea los paquetes hijos en busca de componentes</li>
 * </ul>
 *
 * @author DarÃ­o MÃ¡rquez Bautista
 * @version 0.1.0
 */
@SpringBootApplication
@EnableAsync
public class TowerOfWonderApplication {

    /**
     * MÃ©todo principal â€” arranca el contenedor Spring con el servidor Tomcat embebido.
     *
     * @param args argumentos de lÃ­nea de comandos (no usados en este proyecto)
     */
    public static void main(String[] args) {
        SpringApplication.run(TowerOfWonderApplication.class, args);
    }
}

