# Decisiones Técnicas — Tower of Wonder TFG

> Registro de decisiones de arquitectura (ADR — Architecture Decision Records).
> Cada decisión documenta el contexto, las opciones consideradas, la elección y sus consecuencias.
> Útil para la memoria y defensa del TFG: demuestra que las decisiones son razonadas, no arbitrarias.

---

## ADR-001 — Stack tecnológico principal

**Fecha:** Abril 2026  
**Estado:** ✅ Aprobado

### Contexto
Proyecto TFG: web informativa sobre un videojuego. Necesita sistema de usuarios, dashboard, tienda y newsletter. El proyecto partía de un backend PHP legacy.

### Decisión
- **Frontend:** React 18 + Vite 5 (SPA desacoplada)
- **Backend:** Spring Boot 3.3.5 (API REST)
- **Seguridad:** Spring Security + JWT
- **Base de datos:** MariaDB 10.6
- **ORM:** JPA / Hibernate
- **Contenedores:** Docker + Docker Compose

### Alternativas descartadas
| Alternativa | Por qué se descartó |
|---|---|
| Mantener PHP puro | No cumple el requisito de Spring Boot del TFG |
| Next.js + API Routes | Sobrecarga para un proyecto académico; Spring Boot es más apropiado para el ámbito Java/Backend |
| MongoDB | El modelo de datos es relacional (usuarios, roles, pedidos); SQL es más adecuado |
| Kubernetes | Sobreingeniería para un TFG |

### Consecuencias
- Mayor complejidad inicial (dos lenguajes: Java + JavaScript)
- Mejor separación de responsabilidades (frontend desacoplado del backend)
- Stack ampliamente usado en el mercado laboral → valor curricular

---

## ADR-002 — JWT en lugar de sesiones HTTP

**Fecha:** Abril 2026  
**Estado:** ✅ Aprobado

### Contexto
La autenticación original en PHP usaba sesiones HTTP (`$_SESSION`). Al migrar a una API REST, las sesiones HTTP no son apropiadas.

### Decisión
Usar **JWT (JSON Web Tokens)** con Spring Security.

### Por qué JWT
- **Stateless:** el servidor no almacena estado de sesión; el token contiene toda la información necesaria
- **Escalable:** funciona con múltiples instancias del servidor sin sesiones compartidas
- **Estándar:** RFC 7519, ampliamente adoptado en APIs REST
- **Compatible con SPA:** React puede almacenar el token y enviarlo en cada petición

### Biblioteca elegida
`io.jsonwebtoken:jjwt` (versión 0.12.6) — la más usada en el ecosistema Spring.

### Limitación conocida y cómo se resuelve
Los JWT son stateless, por lo que no se pueden invalidar antes de su expiración. Para el logout, se usa una **tabla `jwt_blacklist`** que almacena los IDs de tokens revocados.

---

## ADR-003 — 2FA con TOTP (Google Authenticator)

**Fecha:** Abril 2026  
**Estado:** ✅ Aprobado

### Contexto
El sistema PHP ya implementaba 2FA con la librería `PHPGangsta_GoogleAuthenticator` (TOTP/RFC 6238). Los secretos Base32 están almacenados en la columna `twofa_secret` de la tabla `usuarios`.

### Decisión
Mantener TOTP en Spring Boot usando `com.warrenstrange:googleauth`.

### Por qué esta librería
- Equivalente Java exacto de la librería PHP usada
- **Los secrets Base32 existentes en la BD son 100% compatibles** — los usuarios no necesitan reconfigurar Google Authenticator
- Implementa RFC 6238 correctamente con ventana de tolerancia de ±30 segundos

### Alternativas descartadas
| Alternativa | Por qué se descartó |
|---|---|
| 2FA por email (OTP) | Requiere servidor SMTP disponible en el momento del login; menos seguro |
| SMS | Coste económico, complejidad de integración |
| FIDO2/WebAuthn | Sobreingeniería para un TFG |

---

## ADR-004 — Maven en Docker (sin instalación local)

**Fecha:** Mayo 2026  
**Estado:** ✅ Aprobado

### Contexto
El desarrollador no tiene Maven instalado localmente (ni se quiere instalar). El proyecto debe compilarse sin dependencias locales.

### Decisión
Build **multi-stage en Docker** usando la imagen oficial `maven:3.9-eclipse-temurin-21`.

```dockerfile
FROM maven:3.9-eclipse-temurin-21 AS builder
# Maven compila aquí — solo existe en el build, no en la imagen final

FROM eclipse-temurin:21-jre
# Solo el JRE y el JAR resultante
```

### Ventajas
- Reproducible en cualquier máquina que tenga Docker
- Sin contaminación del entorno local
- La imagen final pesa ~200MB (solo JRE + JAR) en lugar de ~600MB con JDK

---

## ADR-005 — MapStruct para mapeo Entity ↔ DTO

**Fecha:** Mayo 2026  
**Estado:** ✅ Aprobado

### Contexto
Es necesario convertir entidades JPA (con datos internos como `password_hash`) a DTOs seguros para enviar al cliente, y viceversa.

### Decisión
Usar **MapStruct** como procesador de anotaciones para generar código de mapeo en tiempo de compilación.

### Por qué MapStruct y no otras opciones
| Opción | Problema |
|---|---|
| Mapeo manual | Repetitivo, propenso a errores, no escala |
| ModelMapper (reflection) | Más lento (reflection en runtime), más difícil de depurar |
| **MapStruct** ✅ | Genera código Java en compilación, sin reflection, detectable en tiempo de compilación |

---

## ADR-006 — Thymeleaf para templates de email

**Fecha:** Mayo 2026  
**Estado:** ✅ Aprobado (se implementa en Fase 4)

### Contexto
Los emails (bienvenida, newsletter, alertas de seguridad) necesitan formato HTML. Escribir HTML directamente en cadenas Java es ilegible e inmantenible.

### Decisión
Usar **Thymeleaf** como motor de plantillas para generar el HTML de los emails.

### Por qué Thymeleaf
- Es parte del ecosistema Spring Boot (sin configuración extra)
- Permite usar variables en las plantillas: `${username}`, `${confirmationLink}`
- Los templates son archivos `.html` normales, editables sin tocar Java

---

## ADR-007 — Migración progresiva (PHP → Spring Boot)

**Fecha:** Mayo 2026  
**Estado:** ✅ Aprobado

### Contexto
Reescribir el sistema completo de golpe es arriesgado: si algo falla, el sistema entero deja de funcionar.

### Decisión
Migración **módulo por módulo**, con PHP y Spring Boot coexistiendo temporalmente:

```
nginx actúa como router:
  /api/**  → Spring Boot (módulos ya migrados)
  resto    → PHP (módulos pendientes de migración)
```

### Fases de migración
Ver `MIGRATION.md` para el detalle completo.

### Ventajas
- El sistema nunca deja de funcionar completamente
- Se puede validar cada módulo antes de migrar el siguiente
- Rollback fácil: reapuntar nginx al PHP para una ruta concreta

---

## ADR-008 — Apache POI para exportación Excel

**Fecha:** Mayo 2026  
**Estado:** ✅ Aprobado (se implementa en Fase 3)

### Contexto
El dashboard necesita exportar métricas a Excel.

### Decisión
Usar **Apache POI** (`poi-ooxml` versión 5.3.0) para generar archivos `.xlsx`.

### Por qué Apache POI
- Estándar de facto en el ecosistema Java para manipulación de archivos Office
- Soporte completo de `.xlsx` (formato moderno)
- Alternativas como JExcel o EasyExcel tienen menor adopción o requieren dependencias adicionales

---

## ADR-009 — springdoc-openapi para Swagger UI

**Fecha:** Mayo 2026  
**Estado:** ✅ Aprobado

### Contexto
La API REST necesita documentación interactiva.

### Decisión
Usar **springdoc-openapi** (versión 2.6.0) en lugar del antiguo `springfox`.

### Por qué springdoc y no springfox
| Criterio | springfox | springdoc |
|---|---|---|
| Compatibilidad Spring Boot 3 | ❌ No compatible | ✅ Nativo |
| Mantenimiento activo | ❌ Abandonado | ✅ Activo |
| Spring MVC | ✅ | ✅ |

### Acceso
`http://localhost:8090/swagger-ui.html` (durante desarrollo)
