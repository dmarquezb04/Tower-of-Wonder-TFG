# Tower of Wonder — TFG

Proyecto web desarrollado como Trabajo de Fin de Grado (TFG) del ciclo formativo de Desarrollo de Aplicaciones Web (DAW).

_Tower of Wonder_ es la web oficial de un videojuego ficticio, aún en desarrollo, diseñada como una Single Page Application (SPA) moderna, segura y escalable. El sistema ha sido migrado íntegramente de una arquitectura legacy PHP a un stack moderno basado en **Spring Boot** y **React**.

---

## 🚀 Tecnologías y Arquitectura

La aplicación utiliza una arquitectura de microservicios orquestada con **Docker Compose**.

### Backend (Java & Spring Boot)

- **Framework**: Spring Boot 3.3.5 (Java 21).
- **Seguridad**: Spring Security 6, JWT (JJWT), BCrypt.
- **2FA**: Google Authenticator TOTP (com.warrenstrange).
- **Persistencia**: Spring Data JPA, MariaDB 10.6, H2 (Database en memoria para tests).
- **Mapeo y Boilerplate**: Lombok.
- **Comunicaciones**: Spring Mail + Thymeleaf (Plantillas de email dinámicas).
- **Herramientas**: Apache POI (Exportación Excel), Spring AOP (Logging), Spring Cache.
- **Documentación**: SpringDoc OpenAPI (Swagger UI).

### Frontend (React)

- **Core**: React 18, Vite 5.
- **Enrutado**: React Router v6.
- **Cliente API**: Axios con interceptores globales para gestión de errores.
- **Utilidades**: Context API (Estado global), QRCode.react (Generación de códigos 2FA).
- **Estilos**: CSS Moderno con variables y diseño responsive.

### Infraestructura y Despliegue

- **Contenedores**: Docker & Docker Compose.
- **Servidor Web**: Nginx (Proxy Inverso y servidor de estáticos).
- **CI/CD**: Dockerfiles multi-etapa para optimización de imágenes.

---

## 🏗️ Estructura del Proyecto

```text
Tower-of-Wonder-TFG/
├── frontend/             # Código fuente de React (Vite)
│   ├── src/              # Componentes, Hooks, Contextos, API
│   └── Dockerfile        # Build automatizado de React + Nginx
├── spring/               # Backend Java Spring Boot
│   ├── src/main/java/    # Controladores, Servicios, Entidades, Seguridad
│   ├── pom.xml           # Dependencias de Maven
│   └── Dockerfile        # Build multi-stage de Java
├── docker/               # Configuraciones de infraestructura
│   ├── nginx/            # Configuración del proxy inverso
│   └── db/               # Scripts de inicialización SQL (init.sql)
├── docker-compose.yml    # Orquestación de contenedores
├── .env                  # Variables de entorno (Configuración)
└── MIGRATION.md          # Registro detallado de la migración PHP → Spring
```

---

## 🛠️ Funcionalidades Principales

### 1. Sistema de Autenticación y Seguridad

- **JWT**: Autenticación stateless mediante tokens seguros.
- **2FA (Doble Factor)**: Integración con Google Authenticator mediante códigos TOTP.
- **Roles**: Control de acceso granular (`USER`, `ADMIN`).
- **Audit Logs**: Registro automático de operaciones críticas mediante AOP.

### 2. Tienda y Gestión de Contenidos

- Catálogo dinámico con filtrado por categorías.
- Carrito de compra reactivo persistente.
- Panel de administración: Gestión de productos, usuarios y exportación de métricas a Excel.

### 3. Dashboard y Métricas

- Estadísticas de uso en tiempo real.
- Tracking de actividad y geolocalización de IPs.

---

## 📦 Guía de Inicio (Docker)

El proyecto está completamente automatizado. No necesitas Java, Node ni MariaDB instalados localmente.

### 1. Configuración Inicial

Copia el archivo de ejemplo y edita las variables (DB, Mail, JWT Secret):

> [!TIP]
> Para generar un `JWT_SECRET` seguro en producción, puedes usar el comando: `openssl rand -base64 32`

```bash
cp .env.example .env
```

### 2. Despliegue Completo

Levanta todos los servicios (Base de Datos, Backend y Frontend):

```bash
docker compose up -d --build
```

_Nota: El primer build tardará unos minutos mientras se descargan las dependencias de Maven y NPM dentro de los contenedores._

### 3. Comandos Útiles

- **Ver logs**: `docker compose logs -f`
- **Parar todo**: `docker compose down`
- **Reconstruir solo el backend**: `docker compose up -d --build spring`
- **Ejecutar comandos node (ej. instalar librería)**:
  `docker compose run --rm node npm install <package-name>`

### Servicios Disponibles:

- **Frontend/App**: [http://localhost:8080](http://localhost:8080)
- **API Swagger UI**: [http://localhost:8090/swagger-ui/index.html](http://localhost:8090/swagger-ui/index.html)
- **Base de Datos**: Puerto `3306`
