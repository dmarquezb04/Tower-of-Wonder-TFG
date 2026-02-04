<?php
/**
 * SessionHelper
 * Gestiona todas las operaciones relacionadas con sesiones de usuario
 * 
 * @package TowerOfWonder\Helpers
 * @author Darío Márquez Bautista
 */

require_once __DIR__ . '/../models/AccessLog.php';
require_once __DIR__ . '/../models/Session.php';
require_once __DIR__ . '/../models/Role.php';

class SessionHelper
{
    /**
     * Inicia la sesión si no está iniciada
     * Configura parámetros de seguridad
     * 
     * @return void
     */
    public static function start()
    {
        if (session_status() === PHP_SESSION_NONE) {
            // Configuración de seguridad de la sesión
            ini_set('session.cookie_httponly', 1);  // Prevenir acceso desde JavaScript
            ini_set('session.cookie_secure', 0);    // Cambiar a 1 en producción con HTTPS
            ini_set('session.use_only_cookies', 1); // Solo usar cookies
            ini_set('session.cookie_samesite', 'Lax'); // Protección CSRF
            
            session_start();
        }
    }

    /**
     * Inicia sesión para un usuario
     * Guarda los datos del usuario en la sesión
     * 
     * @param object $usuario Instancia de Usuario
     * @return void
     */
    public static function login($usuario)
    {
        self::start();
        
        // Regenerar ID de sesión por seguridad (prevenir session fixation)
        session_regenerate_id(true);
        
        // Guardar datos del usuario en sesión
        $_SESSION['user_id'] = $usuario->id_usuario;
        $_SESSION['user_email'] = $usuario->email;
        $_SESSION['username'] = $usuario->username;
        $_SESSION['logged_in'] = true;
        $_SESSION['login_time'] = time();
        $_SESSION['two_fa_verified'] = !$usuario->two_fa_enabled; // Si no tiene 2FA, ya está verificado
        
        // Crear sesión en base de datos y guardar token
        $token = Session::crear($usuario->id_usuario);
        if ($token) {
            $_SESSION['session_token'] = $token;
        }
        
        // Registrar login en logs de acceso
        AccessLog::registrar($usuario->id_usuario, AccessLog::ACTION_LOGIN);
        
        // Actualizar último login en la BD
        require_once __DIR__ . '/../models/Usuario.php';
        Usuario::updateLastLogin($usuario->id_usuario);
    }

    /**
     * Cierra la sesión del usuario
     * Destruye todas las variables de sesión
     * 
     * @return void
     */
    public static function logout()
    {
        self::start();
        
        // Registrar logout antes de destruir la sesión
        $userId = self::getUserId();
        if ($userId) {
            AccessLog::registrar($userId, AccessLog::ACTION_LOGOUT);
        }
        
        // Eliminar sesión de la base de datos
        if (isset($_SESSION['session_token'])) {
            Session::eliminar($_SESSION['session_token']);
        }
        
        // Destruir todas las variables de sesión
        $_SESSION = [];
        
        // Destruir la cookie de sesión
        if (isset($_COOKIE[session_name()])) {
            $params = session_get_cookie_params();
            setcookie(
                session_name(),
                '',
                time() - 42000,
                $params['path'],
                $params['domain'],
                $params['secure'],
                $params['httponly']
            );
        }
        
        // Destruir la sesión
        session_destroy();
    }

    /**
     * Verifica si hay un usuario autenticado
     * 
     * @return bool true si está autenticado, false si no
     */
    public static function isAuthenticated()
    {
        self::start();
        
        return isset($_SESSION['logged_in']) && 
               $_SESSION['logged_in'] === true &&
               isset($_SESSION['user_id']);
    }

    /**
     * Verifica si el usuario ha completado la verificación 2FA
     * 
     * @return bool true si está verificado o no necesita 2FA
     */
    public static function is2FAVerified()
    {
        self::start();
        
        return isset($_SESSION['two_fa_verified']) && 
               $_SESSION['two_fa_verified'] === true;
    }

    /**
     * Marca la verificación 2FA como completada
     * 
     * @return void
     */
    public static function mark2FAVerified()
    {
        self::start();
        $_SESSION['two_fa_verified'] = true;
    }

    /**
     * Obtiene el ID del usuario actual
     * 
     * @return int|null ID del usuario o null si no está autenticado
     */
    public static function getUserId()
    {
        self::start();
        return $_SESSION['user_id'] ?? null;
    }

    /**
     * Obtiene el email del usuario actual
     * 
     * @return string|null Email del usuario o null si no está autenticado
     */
    public static function getUserEmail()
    {
        self::start();
        return $_SESSION['user_email'] ?? null;
    }

    /**
     * Obtiene el nombre de usuario actual
     * 
     * @return string|null Nombre de usuario o null si no está autenticado
     */
    public static function getUsername()
    {
        self::start();
        return $_SESSION['username'] ?? null;
    }

    /**
     * Obtiene el tiempo de inicio de sesión
     * 
     * @return int|null Timestamp de inicio de sesión o null
     */
    public static function getLoginTime()
    {
        self::start();
        return $_SESSION['login_time'] ?? null;
    }

    /**
     * Verifica si la sesión ha expirado por inactividad
     * 
     * @param int $timeout Tiempo de expiración en segundos (por defecto 1 hora)
     * @return bool true si ha expirado
     */
    public static function isExpired($timeout = 3600)
    {
        $loginTime = self::getLoginTime();
        
        if ($loginTime === null) {
            return true;
        }
        
        return (time() - $loginTime) > $timeout;
    }

    /**
     * Requiere autenticación, redirige al login si no está autenticado
     * 
     * @param string $redirectTo URL de redirección si no está autenticado
     * @return void
     */
    public static function requireAuth($redirectTo = '/PROYECTO/public/index.php')
    {
        if (!self::isAuthenticated()) {
            header('Location: ' . $redirectTo . '?error=no_autenticado&modal=login');
            exit;
        }
    }

    /**
     * Requiere verificación 2FA completa
     * 
     * @param string $redirectTo URL de redirección si no está verificado
     * @return void
     */
    public static function require2FA($redirectTo = '/PROYECTO/public/auth/verificar_2fa.php')
    {
        self::requireAuth();
        
        if (!self::is2FAVerified()) {
            header('Location: ' . $redirectTo);
            exit;
        }
    }

    /**
     * Obtiene todas las sesiones activas del usuario actual
     * 
     * @return array Lista de sesiones con información del dispositivo
     */
    public static function getSesionesActivas()
    {
        $userId = self::getUserId();
        if (!$userId) {
            return [];
        }
        
        $sesiones = Session::obtenerSesionesActivas($userId);
        
        // Añadir información legible del dispositivo
        foreach ($sesiones as &$sesion) {
            $sesion['dispositivo'] = Session::obtenerNombreDispositivo($sesion['user_agent']);
            $sesion['es_actual'] = isset($_SESSION['session_token']) && 
                                   $_SESSION['session_token'] === $sesion['token_sesion'];
        }
        
        return $sesiones;
    }

    /**
     * Cierra una sesión específica por su token
     * 
     * @param string $token Token de la sesión a cerrar
     * @return bool true si se cerró correctamente
     */
    public static function cerrarSesion($token)
    {
        $userId = self::getUserId();
        if (!$userId) {
            return false;
        }
        
        // Verificar que la sesión pertenece al usuario actual
        $sesiones = Session::obtenerSesionesActivas($userId);
        $encontrada = false;
        
        foreach ($sesiones as $sesion) {
            if ($sesion['token_sesion'] === $token) {
                $encontrada = true;
                break;
            }
        }
        
        if (!$encontrada) {
            return false;
        }
        
        return Session::eliminar($token);
    }

    /**
     * Cierra todas las sesiones excepto la actual
     * Útil para "cerrar sesión en otros dispositivos"
     * 
     * @return int Número de sesiones cerradas
     */
    public static function cerrarOtrasSesiones()
    {
        $userId = self::getUserId();
        if (!$userId) {
            return 0;
        }
        
        $tokenActual = $_SESSION['session_token'] ?? null;
        $sesiones = Session::obtenerSesionesActivas($userId);
        $cerradas = 0;
        
        foreach ($sesiones as $sesion) {
            if ($sesion['token_sesion'] !== $tokenActual) {
                if (Session::eliminar($sesion['token_sesion'])) {
                    $cerradas++;
                }
            }
        }
        
        return $cerradas;
    }

    /**
     * Cuenta el número de sesiones activas del usuario actual
     * 
     * @return int Número de sesiones
     */
    public static function contarSesionesActivas()
    {
        $userId = self::getUserId();
        if (!$userId) {
            return 0;
        }
        
        return Session::contarSesionesActivas($userId);
    }

    /**
     * Verifica si el usuario actual tiene un rol específico
     * 
     * @param string $nombreRol Nombre del rol (usar constantes de Role)
     * @return bool true si tiene el rol
     */
    public static function hasRole($nombreRol)
    {
        $userId = self::getUserId();
        if (!$userId) {
            return false;
        }
        
        return Role::tieneRol($userId, $nombreRol);
    }

    /**
     * Verifica si el usuario actual es administrador
     * 
     * @return bool true si es admin
     */
    public static function isAdmin()
    {
        return self::hasRole(Role::ROLE_ADMIN);
    }

    /**
     * Verifica si el usuario actual es moderador
     * 
     * @return bool true si es moderador
     */
    public static function isModerator()
    {
        return self::hasRole(Role::ROLE_MODERATOR);
    }

    /**
     * Obtiene todos los roles del usuario actual
     * 
     * @return array Lista de roles
     */
    public static function getRoles()
    {
        $userId = self::getUserId();
        if (!$userId) {
            return [];
        }
        
        return Role::obtenerRolesDeUsuario($userId);
    }

    /**
     * Requiere que el usuario tenga un rol específico
     * Redirige si no tiene el rol
     * 
     * @param string $nombreRol Nombre del rol requerido
     * @param string $redirectTo URL de redirección si no tiene permisos
     * @return void
     */
    public static function requireRole($nombreRol, $redirectTo = null)
    {
        self::requireAuth();
        
        if (!self::hasRole($nombreRol)) {
            if ($redirectTo === null) {
                require_once __DIR__ . '/../../config/config.php';
                $redirectTo = BASE_URL . 'index.php?error=sin_permisos';
            }
            header('Location: ' . $redirectTo);
            exit;
        }
    }

    /**
     * Requiere que el usuario sea administrador
     * Redirige si no es admin
     * 
     * @param string $redirectTo URL de redirección si no es admin
     * @return void
     */
    public static function requireAdmin($redirectTo = null)
    {
        self::requireRole(Role::ROLE_ADMIN, $redirectTo);
    }
}
