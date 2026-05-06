# Tower of Wonder — TFG

Proyecto web desarrollado como Trabajo de Fin de Grado (TFG) del ciclo formativo de Desarrollo de Aplicaciones Web (DAW).

*Tower of Wonder* es la web oficial de un videojuego ficticio, diseñada como una Single Page Application (SPA) moderna, segura y escalable. El sistema ha sido migrado íntegramente de una arquitectura legacy PHP a un stack moderno basado en **Spring Boot** y **React**.

---

## 🚀 Tecnologías y Arquitectura

La aplicación utiliza una arquitectura de microservicios orquestada con **Docker Compose**.

### Stack Tecnológico
- **Frontend**: React 18, Vite 5, React Router v6, Axios.
- **Backend**: Java 21, Spring Boot 3.3.5, Spring Security 6 (JWT).
- **Base de Datos**: MariaDB 10.6.
- **Infraestructura**: Nginx (Proxy Inverso), Docker, Docker Compose.
- **Seguridad**: JWT (JSON Web Tokens), BCrypt, Doble Factor de Autenticación (TOTP).

---

## 🏗️ Estructura del Proyecto

```text
Tower-of-Wonder-TFG/
├── frontend/             # Código fuente de React (Vite)
│   ├── src/              # Componentes, Hooks, Contextos, API
│   └── dist/             # Build estático (servido por Nginx)
├── spring/               # Backend Java Spring Boot
│   ├── src/main/java/    # Controladores, Servicios, Entidades, Seguridad
│   └── pom.xml           # Dependencias de Maven
├── docker/               # Configuraciones de infraestructura
│   ├── nginx/            # Configuración del proxy inverso
│   └── db/               # Scripts de inicialización SQL
├── docker-compose.yml    # Orquestación de contenedores
└── MIGRATION.md          # Registro detallado de la migración PHP → Spring
```

---

## 🛠️ Funcionalidades Principales

### 1. Sistema de Autenticación y Seguridad
- **JWT**: Autenticación sin estado (stateless) mediante tokens persistidos en el cliente.
- **2FA (Doble Factor)**: Integración con aplicaciones como Google Authenticator mediante códigos TOTP.
- **Roles**: Control de acceso basado en roles (`USER`, `ADMIN`).
- **Blacklist**: Sistema de revocación de tokens para logout seguro.

### 2. Tienda y Gestión de Contenidos
- Catálogo de productos dinámico filtrable por categorías.
- Carrito de compra reactivo (Context API).
- Panel de administración para gestión de productos y categorías.

### 3. Métricas y Auditoría
- **AOP (Aspect Oriented Programming)**: Logging automático de todas las operaciones de la API.
- **Tracking**: Registro de visitas y estadísticas de uso en tiempo real.

---

## 📦 Despliegue con Docker

El proyecto está completamente dockerizado. Para arrancar el entorno completo:

1. Copia el archivo de ejemplo de variables de entorno:
   ```bash
   cp .env.example .env
   ```
2. Configura las credenciales de base de datos y JWT en el `.env`.
3. Levanta los contenedores:
   ```bash
   docker compose up -d
   ```

### Servicios Disponibles:
- **Frontend/API**: `http://localhost:8080` (vía Nginx)
- **Spring Boot Directo**: `http://localhost:8090` (incluye Swagger UI)
- **Base de Datos**: Puerto `3306`

---

## 📝 Documentación de la Migración

Para detalles técnicos sobre el proceso de migración desde el sistema antiguo en PHP al nuevo backend en Java, consulta el archivo [MIGRATION.md](MIGRATION.md).

---

## Estado del Proyecto
✅ **Migración Completada**. El sistema es 100% funcional sobre la nueva arquitectura Spring Boot + React.
