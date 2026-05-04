package com.tow.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Punto de entrada de la aplicación Spring Boot — Tower of Wonder Backend.
 *
 * <p>{@code @SpringBootApplication} es una meta-anotación que combina:
 * <ul>
 *   <li>{@code @Configuration} — marca esta clase como fuente de beans Spring</li>
 *   <li>{@code @EnableAutoConfiguration} — activa la autoconfiguración de Spring Boot</li>
 *   <li>{@code @ComponentScan} — escanea los paquetes hijos en busca de componentes</li>
 * </ul>
 *
 * @author Darío Márquez Bautista
 * @version 0.1.0
 */
@SpringBootApplication
@EnableAsync
public class TowerOfWonderApplication {

    /**
     * Método principal — arranca el contenedor Spring con el servidor Tomcat embebido.
     *
     * @param args argumentos de línea de comandos (no usados en este proyecto)
     */
    public static void main(String[] args) {
        SpringApplication.run(TowerOfWonderApplication.class, args);
    }
}
