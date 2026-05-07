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
 * Aspecto AOP que intercepta todos los mÃ©todos de la capa de servicio
 * y genera trazas de log automÃ¡ticamente sin modificar el cÃ³digo de negocio.
 *
 * <p>Â¿QuÃ© es AOP (ProgramaciÃ³n Orientada a Aspectos)?
 * Es una tÃ©cnica que permite aÃ±adir comportamiento transversal (cross-cutting concerns)
 * como logging, mÃ©tricas o transacciones, sin mezclar ese cÃ³digo con la lÃ³gica de negocio.
 * En lugar de aÃ±adir {@code log.info()} en cada mÃ©todo, el aspecto lo hace automÃ¡ticamente.
 *
 * <p>Este aspecto intercepta todos los mÃ©todos de los paquetes {@code service} y {@code controller},
 * registrando:
 * <ul>
 *   <li>Entrada al mÃ©todo con sus parÃ¡metros (excepto campos sensibles como contraseÃ±as)</li>
 *   <li>Salida del mÃ©todo con el tiempo de ejecuciÃ³n en milisegundos</li>
 *   <li>Excepciones con el mensaje de error</li>
 * </ul>
 *
 * <p>Los logs se escriben en:
 * <ul>
 *   <li>Consola (en desarrollo)</li>
 *   <li>{@code logs/tow-app.log} â€” todos los eventos</li>
 *   <li>{@code logs/tow-error.log} â€” solo errores</li>
 * </ul>
 * Ver {@code logback-spring.xml} para la configuraciÃ³n completa.
 *
 * @author DarÃ­o MÃ¡rquez Bautista
 */
@Aspect
@Component
public class LoggingAspect {

    /**
     * Nombres de parÃ¡metros que NUNCA se deben loggear por seguridad.
     * Los parÃ¡metros cuyo nombre coincida con alguno de estos valores
     * se sustituyen por {@code [PROTECTED]} en el log.
     */
    private static final Set<String> SENSITIVE_PARAMS = Set.of(
            "password", "passwordHash", "secret", "twofaSecret", "token", "code"
    );

    /**
     * Pointcut: intercepta todos los mÃ©todos de los services y controllers
     * del paquete {@code com.tow.backend}.
     *
     * <p>Sintaxis del pointcut:
     * <ul>
     *   <li>{@code execution(*)} â€” cualquier tipo de retorno</li>
     *   <li>{@code com.tow.backend..service.*.*} â€” cualquier clase en un subpaquete {@code service}</li>
     *   <li>{@code (..)} â€” cualquier nÃºmero y tipo de parÃ¡metros</li>
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

        // Logger especÃ­fico de la clase que se estÃ¡ interceptando
        Logger log = LoggerFactory.getLogger(signature.getDeclaringType());

        // Sanitizar parÃ¡metros antes de loggear (ocultar datos sensibles)
        String params = buildSafeParamsString(signature, joinPoint.getArgs());

        log.debug("â†’ {}.{}({})", className, methodName, params);
        long startTime = System.currentTimeMillis();

        try {
            Object result = joinPoint.proceed();

            long elapsed = System.currentTimeMillis() - startTime;
            log.debug("â† {}.{}() completado en {} ms", className, methodName, elapsed);

            return result;

        } catch (Exception e) {
            long elapsed = System.currentTimeMillis() - startTime;
            log.error("âœ— {}.{}() fallÃ³ tras {} ms â€” {}: {}",
                    className, methodName, elapsed,
                    e.getClass().getSimpleName(), e.getMessage());
            throw e;
        }
    }

    /**
     * Construye un string legible con los parÃ¡metros del mÃ©todo,
     * sustituyendo los campos sensibles por {@code [PROTECTED]}.
     *
     * @param signature firma del mÃ©todo interceptado
     * @param args      valores reales de los parÃ¡metros
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
     * Determina si un parÃ¡metro es sensible y no debe aparecer en los logs.
     *
     * @param paramName nombre del parÃ¡metro
     * @param arg       valor del parÃ¡metro
     * @return true si el parÃ¡metro no debe loggarse
     */
    private boolean isSensitive(String paramName, Object arg) {
        // Si el nombre del parÃ¡metro es sensible
        if (SENSITIVE_PARAMS.contains(paramName)) {
            return true;
        }
        // Si el objeto tiene campos tÃ­picamente sensibles (DTOs de login)
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
     * @return representaciÃ³n en texto
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

