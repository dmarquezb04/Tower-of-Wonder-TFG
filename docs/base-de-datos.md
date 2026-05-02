# Base de Datos — Tower of Wonder TFG

## Motor
**MariaDB 10.6** — compatible con MySQL. Alojado en contenedor Docker.

---

## Esquema inicial (Fase 0)

### `usuarios`
Tabla principal de usuarios del sistema.

| Columna | Tipo | Descripción |
|---|---|---|
| `id_usuario` | INT PK AUTO_INCREMENT | Identificador único |
| `email` | VARCHAR(255) UNIQUE | Email de acceso (único) |
| `username` | VARCHAR(50)| Nombre público del usuario |
| `password_hash` | VARCHAR(255) | Hash BCrypt de la contraseña |
| `two_fa_enabled` | TINYINT(1) | Si el 2FA está activo (0/1) |
| `twofa_secret` | VARCHAR(255) | Secreto Base32 para Google Authenticator |
| `activo` | TINYINT(1) | Si la cuenta está activa |
| `fecha_creacion` | DATETIME | Fecha de registro |
| `ultimo_login` | DATETIME | Último acceso |

> ℹ️ `password_hash` usa BCrypt (generado con `password_hash()` en PHP). Spring Boot usa `BCryptPasswordEncoder` que es 100% compatible — los usuarios existentes no necesitan cambiar su contraseña.

### `roles`
Roles del sistema. Datos fijos.

| Columna | Tipo | Descripción |
|---|---|---|
| `id_rol` | INT PK | Identificador |
| `nombre_rol` | VARCHAR(50) UNIQUE | `admin`, `moderator`, `user` |
| `descripcion` | VARCHAR(255) | Descripción del rol |

### `usuario_roles`
Relación muchos a muchos entre usuarios y roles.

| Columna | Tipo | Descripción |
|---|---|---|
| `id_usuario` | INT FK | Referencia a `usuarios` |
| `id_rol` | INT FK | Referencia a `roles` |

### `sesiones`
> ⚠️ **Tabla legacy** — usada por PHP. Se mantiene durante la migración pero no se usa en Spring Boot (JWT es stateless).

| Columna | Tipo | Descripción |
|---|---|---|
| `id_sesion` | INT PK | Identificador |
| `id_usuario` | INT FK | Usuario propietario |
| `token_sesion` | VARCHAR(64) UNIQUE | Token de sesión PHP |
| `ip` | VARCHAR(45) | IP del cliente |
| `user_agent` | VARCHAR(255) | Navegador |
| `fecha_inicio` | DATETIME | Inicio de sesión |
| `fecha_expiracion` | DATETIME | Expiración |

### `login_attempts`
Registro de intentos de login para protección anti-fuerza-bruta.

| Columna | Tipo | Descripción |
|---|---|---|
| `id_intento` | INT PK | Identificador |
| `email` | VARCHAR(255) | Email intentado |
| `ip` | VARCHAR(45) | IP del intento |
| `exitoso` | TINYINT(1) | Si el intento fue exitoso |
| `fecha` | DATETIME | Momento del intento |

### `logs_acceso`
Auditoría de acciones de usuarios.

| Columna | Tipo | Descripción |
|---|---|---|
| `id_log` | INT PK | Identificador |
| `id_usuario` | INT FK | Usuario (nullable) |
| `accion` | VARCHAR(50) | `login`, `logout`, etc. |
| `ip` | VARCHAR(45) | IP del cliente |
| `fecha` | DATETIME | Momento de la acción |

### `two_factor_codes`
Códigos OTP de un solo uso (email). Actualmente no se usa (se usa TOTP).

| Columna | Tipo | Descripción |
|---|---|---|
| `id_codigo` | INT PK | Identificador |
| `id_usuario` | INT FK | Usuario propietario |
| `codigo` | VARCHAR(10) | Código generado |
| `fecha_creacion` | DATETIME | Cuándo se generó |
| `fecha_expiracion` | DATETIME | Cuándo expira |
| `usado` | TINYINT(1) | Si ya fue usado |

### `two_factor_config`
Configuración de 2FA por usuario.

| Columna | Tipo | Descripción |
|---|---|---|
| `id_usuario` | INT PK FK | Usuario |
| `tipo_2fa` | ENUM('email') | Tipo de 2FA configurado |
| `activo` | TINYINT(1) | Si está activo |
| `fecha_activacion` | DATETIME | Cuándo se activó |

---

## Tablas añadidas en Fase 1

### `jwt_blacklist`
Tokens JWT revocados (para soporte de logout real).

| Columna | Tipo | Descripción |
|---|---|---|
| `id` | INT PK AUTO_INCREMENT | Identificador |
| `token_jti` | VARCHAR(64) UNIQUE | ID único del token (claim `jti`) |
| `fecha_expiracion` | DATETIME | Cuándo expira el token (para limpiar la tabla) |
| `fecha_revocacion` | DATETIME | Cuándo se añadió a la blacklist |

> ℹ️ **Por qué existe esta tabla:** los JWT son stateless y no se pueden invalidar antes de su expiración. Al hacer logout, en lugar de borrar el token del cliente, se añade su `jti` (JWT ID) a esta tabla. Cualquier petición con un token en la blacklist es rechazada, incluso si el token es válido.

---

## Tablas añadidas en Fase 3

```sql
-- Métricas del dashboard
CREATE TABLE page_views (
  id_view INT AUTO_INCREMENT PRIMARY KEY,
  path VARCHAR(255),
  ip VARCHAR(45),
  country_code VARCHAR(3),
  id_usuario INT DEFAULT NULL,
  fecha DATETIME DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (id_usuario) REFERENCES usuarios(id_usuario) ON DELETE SET NULL
);

-- Catálogo de la tienda
CREATE TABLE products (
  id_product INT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(100) NOT NULL,
  description TEXT,
  price DECIMAL(10,2) NOT NULL,
  stock INT DEFAULT 0,
  image_url VARCHAR(255),
  activo TINYINT(1) DEFAULT 1,
  fecha_creacion DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- Carrito de compra
CREATE TABLE cart_items (
  id_item INT AUTO_INCREMENT PRIMARY KEY,
  id_usuario INT NOT NULL,
  id_product INT NOT NULL,
  quantity INT DEFAULT 1,
  FOREIGN KEY (id_usuario) REFERENCES usuarios(id_usuario) ON DELETE CASCADE,
  FOREIGN KEY (id_product) REFERENCES products(id_product) ON DELETE CASCADE
);
```

## Tablas añadidas en Fase 4

```sql
-- Suscriptores a la newsletter
CREATE TABLE newsletter_subscribers (
  id_subscriber INT AUTO_INCREMENT PRIMARY KEY,
  email VARCHAR(255) NOT NULL UNIQUE,
  token_confirmacion VARCHAR(64),
  confirmado TINYINT(1) DEFAULT 0,
  activo TINYINT(1) DEFAULT 1,
  fecha_suscripcion DATETIME DEFAULT CURRENT_TIMESTAMP,
  fecha_baja DATETIME DEFAULT NULL
);
```

---

## Relaciones entre tablas

```
usuarios (1) ──────────── (N) usuario_roles (N) ──────────── (1) roles
usuarios (1) ──────────── (N) sesiones             [legacy PHP]
usuarios (1) ──────────── (N) login_attempts
usuarios (1) ──────────── (N) logs_acceso
usuarios (1) ──────────── (N) two_factor_codes
usuarios (1) ──────────── (1) two_factor_config
usuarios (1) ──────────── (N) cart_items (N) ──── (1) products
```
