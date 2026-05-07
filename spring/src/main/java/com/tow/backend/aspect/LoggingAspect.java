package com.tow.backend.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Set;

/**
 * Aspecto AOP que intercepta todos los métodos de la capa de servicio
 * y genera trazas de log automáticamente sin modificar el código de negocio.
 *
 * <p>¿Qué es AOP (Programación Orientada a Aspectos)?
 * Es una técnica que permite añadir comportamiento transversal (cross-cutting concerns)
 * como logging, métricas o transacciones, sin mezclar ese código con la lógica de negocio.
 * En lugar de añadir {@code log.info()} en cada método, el aspecto lo hace automáticamente.
 *
 * <p>Este aspecto intercepta todos los métodos de los paquetes {@code service} y {@code controller},
 * registrando:
 * <ul>
 *   <li>Entrada al método con sus parámetros (excepto campos sensibles como contraseñas)</li>
 *   <li>Salida del método con el tiempo de ejecución en milisegundos</li>
 *   <li>Excepciones con el mensaje de error</li>
 * </ul>
 *
 * <p>Los logs se escriben en:
 * <ul>
 *   <li>Consola (en desarrollo)</li>
 *   <li>{@code logs/tow-app.log} — todos los eventos</li>
 *   <li>{@code logs/tow-error.log} — solo errores</li>
 * </ul>
 * Ver {@code logback-spring.xml} para la configuración completa.
 *
 * @author Darío Márquez Bautista
 */
@Aspect
@Component
public class LoggingAspect {

    /**
     * Nombres de parámetros que NUNCA se deben loggear por seguridad.
     * Los parámetros cuyo nombre coincida con alguno de estos valores
     * se sustituyen por {@code [PROTECTED]} en el log.
     */
    private static final Set<String> SENSITIVE_PARAMS = Set.of(
            "password", "passwordHash", "secret", "twofaSecret", "token", "code"
    );

    /**
     * Pointcut: intercepta todos los métodos de los services y controllers
     * del paquete {@code com.tow.backend}.
     *
     * <p>Sintaxis del pointcut:
     * <ul>
     *   <li>{@code execution(*)} — cualquier tipo de retorno</li>
     *   <li>{@code com.tow.backend..service.*.*} — cualquier clase en un subpaquete {@code service}</li>
     *   <li>{@code (..)} — cualquier número y tipo de parámetros</li>
     * </ul>
     */
    @Around(
        "execution(* com.tow.backend..service.*.*(..)) || " +
        "execution(* com.tow.backend..controller.*.*(..))"
    )
    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String className  = signature.getDeclaringType().getSimpleName();
        String methodName = signature.getName();

        // Logger específico de la clase que se está interceptando
        Logger log = LoggerFactory.getLogger(signature.getDeclaringType());

        // Sanitizar parámetros antes de loggear (ocultar datos sensibles)
        String params = buildSafeParamsString(signature, joinPoint.getArgs());

        log.debug("→ {}.{}({})", className, methodName, params);
        long startTime = System.currentTimeMillis();

        try {
            Object result = joinPoint.proceed();

            long elapsed = System.currentTimeMillis() - startTime;
            log.debug("← {}.{}() completado en {} ms", className, methodName, elapsed);

            return result;

        } catch (Exception e) {
            long elapsed = System.currentTimeMillis() - startTime;
            log.error("✘ {}.{}() falló tras {} ms — {}: {}",
                    className, methodName, elapsed,
                    e.getClass().getSimpleName(), e.getMessage());
            throw e;
        }
    }

    /**
     * Construye un string legible con los parámetros del método,
     * sustituyendo los campos sensibles por {@code [PROTECTED]}.
     *
     * @param signature firma del método interceptado
     * @param args      valores reales de los parámetros
     * @return string para incluir en el log
     */
    private String buildSafeParamsString(MethodSignature signature, Object[] args) {
        if (args == null || args.length == 0) {
            return "";
        }

        String[] paramNames = signature.getParameterNames();
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < args.length; i++) {
            if (i > 0) sb.append(", ");

            String paramName = (paramNames != null && i < paramNames.length)
                    ? paramNames[i] : "arg" + i;

            if (isSensitive(paramName, args[i])) {
                sb.append(paramName).append("=[PROTECTED]");
            } else {
                sb.append(paramName).append("=").append(formatArg(args[i]));
            }
        }

        return sb.toString();
    }

    /**
     * Determina si un parámetro es sensible y no debe aparecer en los logs.
     *
     * @param paramName nombre del parámetro
     * @param arg       valor del parámetro
     * @return true si el parámetro no debe loggarse
     */
    private boolean isSensitive(String paramName, Object arg) {
        // Si el nombre del parámetro es sensible
        if (SENSITIVE_PARAMS.contains(paramName)) {
            return true;
        }
        // Si el objeto tiene campos típicamente sensibles (DTOs de login)
        if (arg != null) {
            String className = arg.getClass().getSimpleName().toLowerCase();
            return className.contains("request") && (
                    className.contains("login") ||
                    className.contains("register") ||
                    className.contains("twofactor")
            );
        }
        return false;
    }

    /**
     * Formatea un argumento para el log, limitando colecciones largas.
     *
     * @param arg valor a formatear
     * @return representación en texto
     */
    private String formatArg(Object arg) {
        if (arg == null) return "null";
        if (arg instanceof String s) {
            // Truncar strings muy largos (ej: tokens JWT de 500 chars)
            return s.length() > 50 ? s.substring(0, 47) + "..." : s;
        }
        if (arg.getClass().isArray()) {
            return Arrays.toString((Object[]) arg);
        }
        return arg.toString();
    }
}

