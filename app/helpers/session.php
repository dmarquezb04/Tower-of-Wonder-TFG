<?php
/**
 * SessionHelper
 * Gestiona todas las operaciones relacionadas con sesiones de usuario
 * 
 * @package TowerOfWonder\Helpers
 * @author Darío Márquez Bautista
 */

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
        $_SESSION['logged_in'] = true;
        $_SESSION['login_time'] = time();
        $_SESSION['two_fa_verified'] = !$usuario->two_fa_enabled; // Si no tiene 2FA, ya está verificado
        
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
}
