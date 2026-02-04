# Tower of Wonder

Proyecto web desarrollado como Trabajo de Fin de Grado (TFG) del ciclo formativo de Desarrollo de Aplicaciones Web (DAW).

El objetivo del proyecto es desarrollar la web oficial de un videojuego ficticio llamado *Tower of Wonder*, incorporando funcionalidades avanzadas propias de una aplicación web moderna: autenticación de usuarios, seguridad, base de datos, administración de contenidos y servicios auxiliares.

---

## Arquitectura del proyecto

El proyecto sigue una **arquitectura por capas**, separando responsabilidades para facilitar el mantenimiento, la escalabilidad y la reutilización del código.

### Capas principales

- **Controllers**  
  Reciben las peticiones HTTP desde `public/`, procesan la entrada del usuario y delegan la lógica en los servicios.

- **Services**  
  Contienen la lógica de negocio de la aplicación (autenticación, registro, envío de correos, etc.).  
  No gestionan HTML ni acceso directo a variables globales como `$_POST`.

- **Models**  
  Representan entidades de la base de datos y encapsulan el acceso a los datos.

- **Core**  
  Contiene clases técnicas reutilizables como la conexión a la base de datos y validaciones genéricas.

- **Helpers**  
  Funciones y utilidades transversales como gestión de sesiones y seguridad.

- **Config**  
  Archivos de configuración (base de datos, correo, parámetros globales).

---

## Estructura de directorios

app/
├── config/ # Configuración de la aplicación
├── controllers/ # Controladores (Auth, TwoFactor, etc.)
├── core/ # Núcleo técnico (Database, Validator)
├── helpers/ # Utilidades (session, security)
├── models/ # Modelos de datos
└── services/ # Lógica de negocio

public/
├── auth/ # Endpoints de autenticación
├── assets/ # CSS, JS, imágenes
└── index.php # Punto de entrada principal


---

## Autenticación de usuarios

El sistema de autenticación se implementa siguiendo buenas prácticas de seguridad:

- Login mediante **email y contraseña**
- Contraseñas almacenadas usando `password_hash`
- **Doble factor de autenticación (2FA)** opcional mediante código enviado por email
- Separación clara entre controlador, servicio y modelo

### Flujo de login

1. El formulario (modal o página) envía los datos a `public/auth/login.php`
2. El controlador correspondiente delega la lógica en `AuthService`
3. `AuthService`:
   - Valida los datos
   - Busca el usuario por email
   - Verifica la contraseña
   - Comprueba si el usuario tiene 2FA activado
4. Si el usuario tiene 2FA:
   - Se genera un código temporal
   - Se almacena en base de datos
   - Se envía por correo electrónico
   - El usuario es redirigido a la verificación del código
5. Si no tiene 2FA:
   - El login se considera correcto

---

## Principios aplicados

- Separación de responsabilidades
- Reutilización de código
- Seguridad en la gestión de credenciales
- Código organizado y escalable
- Preparado para futuras ampliaciones (admin, tienda, estadísticas, IA)

---

## Notas para desarrollo

- Los archivos dentro de `app/` **no son accesibles directamente desde el navegador**
- La carpeta `public/` es el único punto de acceso web
- La lógica de negocio nunca se implementa en archivos públicos
- Los servicios devuelven estados, no HTML

---

## Estado del proyecto

Proyecto en desarrollo.
