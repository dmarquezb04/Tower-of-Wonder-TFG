<?php
/**
 * Modelo Session
 * Gestiona sesiones persistentes en base de datos
 * Permite múltiples sesiones por usuario y control remoto
 * 
 * @package TowerOfWonder\Models
 * @author Darío Márquez Bautista
 */

require_once __DIR__ . '/../core/Database.php';

class Session
{
    // Configuración de sesiones
    const EXPIRATION_TIME = 86400;  // 24 horas en segundos
    const TOKEN_LENGTH = 64;         // Longitud del token de sesión

    /**
     * Crea una nueva sesión en la base de datos
     * 
     * @param int $idUsuario ID del usuario
     * @param string|null $ip Dirección IP (se detecta automáticamente si no se proporciona)
     * @param string|null $userAgent User Agent del navegador
     * @return string|false Token de sesión generado o false si falla
     */
    public static function crear($idUsuario, $ip = null, $userAgent = null)
    {
        try {
            // Generar token único y seguro
            $token = self::generarToken();
            
            // Obtener IP y User Agent si no se proporcionan
            if ($ip === null) {
                $ip = self::obtenerIP();
            }
            if ($userAgent === null) {
                $userAgent = $_SERVER['HTTP_USER_AGENT'] ?? 'Unknown';
            }
            
            // Calcular fecha de expiración
            $fechaExpiracion = date('Y-m-d H:i:s', time() + self::EXPIRATION_TIME);
            
            $conexion = Database::getInstance()->getConexion();
            
            $stmt = $conexion->prepare("
                INSERT INTO sesiones (id_usuario, token_sesion, ip, user_agent, fecha_inicio, fecha_expiracion)
                VALUES (?, ?, ?, ?, NOW(), ?)
            ");
            
            $stmt->execute([$idUsuario, $token, $ip, $userAgent, $fechaExpiracion]);
            
            return $token;
            
        } catch (PDOException $e) {
            error_log("Error en Session::crear - " . $e->getMessage());
            return false;
        }
    }

    /**
     * Valida un token de sesión
     * 
     * @param string $token Token de sesión a validar
     * @return array|false Datos de la sesión o false si no es válida
     */
    public static function validar($token)
    {
        try {
            $conexion = Database::getInstance()->getConexion();
            
            $stmt = $conexion->prepare("
                SELECT s.id_sesion, s.id_usuario, s.token_sesion, s.ip, s.user_agent,
                       s.fecha_inicio, s.fecha_expiracion,
                       u.email, u.username, u.activo
                FROM sesiones s
                INNER JOIN usuarios u ON s.id_usuario = u.id_usuario
                WHERE s.token_sesion = ?
                  AND s.fecha_expiracion > NOW()
                  AND u.activo = 1
            ");
            
            $stmt->execute([$token]);
            $sesion = $stmt->fetch();
            
            if (!$sesion) {
                return false;
            }
            
            return $sesion;
            
        } catch (PDOException $e) {
            error_log("Error en Session::validar - " . $e->getMessage());
            return false;
        }
    }

    /**
     * Renueva la fecha de expiración de una sesión
     * 
     * @param string $token Token de sesión
     * @return bool true si se renovó correctamente
     */
    public static function renovar($token)
    {
        try {
            $conexion = Database::getInstance()->getConexion();
            
            $nuevaExpiracion = date('Y-m-d H:i:s', time() + self::EXPIRATION_TIME);
            
            $stmt = $conexion->prepare("
                UPDATE sesiones
                SET fecha_expiracion = ?
                WHERE token_sesion = ?
            ");
            
            return $stmt->execute([$nuevaExpiracion, $token]);
            
        } catch (PDOException $e) {
            error_log("Error en Session::renovar - " . $e->getMessage());
            return false;
        }
    }

    /**
     * Elimina una sesión específica (cierre de sesión)
     * 
     * @param string $token Token de sesión a eliminar
     * @return bool true si se eliminó correctamente
     */
    public static function eliminar($token)
    {
        try {
            $conexion = Database::getInstance()->getConexion();
            
            $stmt = $conexion->prepare("
                DELETE FROM sesiones
                WHERE token_sesion = ?
            ");
            
            return $stmt->execute([$token]);
            
        } catch (PDOException $e) {
            error_log("Error en Session::eliminar - " . $e->getMessage());
            return false;
        }
    }

    /**
     * Elimina todas las sesiones de un usuario
     * Útil para "cerrar sesión en todos los dispositivos"
     * 
     * @param int $idUsuario ID del usuario
     * @return int Número de sesiones eliminadas
     */
    public static function eliminarTodasDelUsuario($idUsuario)
    {
        try {
            $conexion = Database::getInstance()->getConexion();
            
            $stmt = $conexion->prepare("
                DELETE FROM sesiones
                WHERE id_usuario = ?
            ");
            
            $stmt->execute([$idUsuario]);
            return $stmt->rowCount();
            
        } catch (PDOException $e) {
            error_log("Error en Session::eliminarTodasDelUsuario - " . $e->getMessage());
            return 0;
        }
    }

    /**
     * Obtiene todas las sesiones activas de un usuario
     * 
     * @param int $idUsuario ID del usuario
     * @return array Lista de sesiones activas
     */
    public static function obtenerSesionesActivas($idUsuario)
    {
        try {
            $conexion = Database::getInstance()->getConexion();
            
            $stmt = $conexion->prepare("
                SELECT id_sesion, token_sesion, ip, user_agent, fecha_inicio, fecha_expiracion
                FROM sesiones
                WHERE id_usuario = ?
                  AND fecha_expiracion > NOW()
                ORDER BY fecha_inicio DESC
            ");
            
            $stmt->execute([$idUsuario]);
            return $stmt->fetchAll();
            
        } catch (PDOException $e) {
            error_log("Error en Session::obtenerSesionesActivas - " . $e->getMessage());
            return [];
        }
    }

    /**
     * Elimina sesiones expiradas de la base de datos (mantenimiento)
     * Se recomienda ejecutar periódicamente (cron job)
     * 
     * @return int Número de sesiones eliminadas
     */
    public static function limpiarExpiradas()
    {
        try {
            $conexion = Database::getInstance()->getConexion();
            
            $stmt = $conexion->prepare("
                DELETE FROM sesiones
                WHERE fecha_expiracion < NOW()
            ");
            
            $stmt->execute();
            return $stmt->rowCount();
            
        } catch (PDOException $e) {
            error_log("Error en Session::limpiarExpiradas - " . $e->getMessage());
            return 0;
        }
    }

    /**
     * Cuenta el número de sesiones activas de un usuario
     * 
     * @param int $idUsuario ID del usuario
     * @return int Número de sesiones activas
     */
    public static function contarSesionesActivas($idUsuario)
    {
        try {
            $conexion = Database::getInstance()->getConexion();
            
            $stmt = $conexion->prepare("
                SELECT COUNT(*) as total
                FROM sesiones
                WHERE id_usuario = ?
                  AND fecha_expiracion > NOW()
            ");
            
            $stmt->execute([$idUsuario]);
            $result = $stmt->fetch();
            
            return (int) $result['total'];
            
        } catch (PDOException $e) {
            error_log("Error en Session::contarSesionesActivas - " . $e->getMessage());
            return 0;
        }
    }

    /**
     * Genera un token de sesión único y seguro
     * 
     * @return string Token generado
     */
    private static function generarToken()
    {
        return bin2hex(random_bytes(self::TOKEN_LENGTH / 2));
    }

    /**
     * Obtiene la dirección IP del cliente
     * 
     * @return string IP del cliente
     */
    private static function obtenerIP()
    {
        if (!empty($_SERVER['HTTP_CLIENT_IP'])) {
            $ip = $_SERVER['HTTP_CLIENT_IP'];
        } elseif (!empty($_SERVER['HTTP_X_FORWARDED_FOR'])) {
            $ips = explode(',', $_SERVER['HTTP_X_FORWARDED_FOR']);
            $ip = trim($ips[0]);
        } else {
            $ip = $_SERVER['REMOTE_ADDR'] ?? '0.0.0.0';
        }
        
        return $ip;
    }

    /**
     * Obtiene información legible del dispositivo desde el User Agent
     * 
     * @param string $userAgent User Agent del navegador
     * @return string Descripción legible del dispositivo
     */
    public static function obtenerNombreDispositivo($userAgent)
    {
        // Detectar navegador
        if (strpos($userAgent, 'Firefox') !== false) {
            $navegador = 'Firefox';
        } elseif (strpos($userAgent, 'Chrome') !== false) {
            $navegador = 'Chrome';
        } elseif (strpos($userAgent, 'Safari') !== false) {
            $navegador = 'Safari';
        } elseif (strpos($userAgent, 'Edge') !== false) {
            $navegador = 'Edge';
        } else {
            $navegador = 'Navegador desconocido';
        }
        
        // Detectar sistema operativo
        if (strpos($userAgent, 'Windows') !== false) {
            $so = 'Windows';
        } elseif (strpos($userAgent, 'Mac') !== false) {
            $so = 'Mac';
        } elseif (strpos($userAgent, 'Linux') !== false) {
            $so = 'Linux';
        } elseif (strpos($userAgent, 'Android') !== false) {
            $so = 'Android';
        } elseif (strpos($userAgent, 'iOS') !== false) {
            $so = 'iOS';
        } else {
            $so = 'SO desconocido';
        }
        
        return "$navegador en $so";
    }
}
