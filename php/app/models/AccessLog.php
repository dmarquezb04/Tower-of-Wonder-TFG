<?php
/**
 * Modelo AccessLog
 * Gestiona el registro de auditoría de acciones de usuarios
 * 
 * @package TowerOfWonder\Models
 * @author Darío Márquez Bautista
 */

require_once __DIR__ . '/../core/Database.php';

class AccessLog
{
    // Constantes de acciones comunes
    const ACTION_LOGIN = 'login';
    const ACTION_LOGOUT = 'logout';
    const ACTION_REGISTER = 'register';
    const ACTION_PASSWORD_CHANGE = 'password_change';
    const ACTION_2FA_ENABLE = '2fa_enable';
    const ACTION_2FA_DISABLE = '2fa_disable';
    const ACTION_2FA_VERIFY = '2fa_verify';
    const ACTION_PROFILE_UPDATE = 'profile_update';
    const ACTION_EMAIL_CHANGE = 'email_change';

    /**
     * Registra una acción del usuario
     * 
     * @param int $idUsuario ID del usuario que realiza la acción
     * @param string $accion Tipo de acción (usar constantes)
     * @param string|null $ip Dirección IP (opcional, se detecta automáticamente)
     * @return bool true si se registró correctamente
     */
    public static function registrar($idUsuario, $accion, $ip = null)
    {
        try {
            // Si no se proporciona IP, intentar obtenerla
            if ($ip === null) {
                $ip = self::obtenerIP();
            }
            
            $conexion = Database::getInstance()->getConexion();
            
            $stmt = $conexion->prepare("
                INSERT INTO logs_acceso (id_usuario, accion, ip, fecha)
                VALUES (?, ?, ?, NOW())
            ");
            
            return $stmt->execute([$idUsuario, $accion, $ip]);
            
        } catch (PDOException $e) {
            error_log("Error en AccessLog::registrar - " . $e->getMessage());
            return false;
        }
    }

    /**
     * Obtiene el historial de acciones de un usuario
     * 
     * @param int $idUsuario ID del usuario
     * @param int $limit Límite de resultados
     * @param string|null $accion Filtrar por tipo de acción específica
     * @return array Lista de logs
     */
    public static function obtenerPorUsuario($idUsuario, $limit = 50, $accion = null)
    {
        try {
            $conexion = Database::getInstance()->getConexion();
            
            $sql = "
                SELECT id_log, id_usuario, accion, ip, fecha
                FROM logs_acceso
                WHERE id_usuario = ?
            ";
            
            $params = [$idUsuario];
            
            // Filtrar por acción si se especifica
            if ($accion !== null) {
                $sql .= " AND accion = ?";
                $params[] = $accion;
            }
            
            $sql .= " ORDER BY fecha DESC LIMIT ?";
            $params[] = $limit;
            
            $stmt = $conexion->prepare($sql);
            $stmt->execute($params);
            
            return $stmt->fetchAll();
            
        } catch (PDOException $e) {
            error_log("Error en AccessLog::obtenerPorUsuario - " . $e->getMessage());
            return [];
        }
    }

    /**
     * Obtiene todos los logs recientes (para administradores)
     * 
     * @param int $limit Límite de resultados
     * @param string|null $accion Filtrar por tipo de acción
     * @return array Lista de logs
     */
    public static function obtenerRecientes($limit = 100, $accion = null)
    {
        try {
            $conexion = Database::getInstance()->getConexion();
            
            $sql = "
                SELECT l.id_log, l.id_usuario, l.accion, l.ip, l.fecha,
                       u.email, u.username
                FROM logs_acceso l
                LEFT JOIN usuarios u ON l.id_usuario = u.id_usuario
            ";
            
            $params = [];
            
            if ($accion !== null) {
                $sql .= " WHERE l.accion = ?";
                $params[] = $accion;
            }
            
            $sql .= " ORDER BY l.fecha DESC LIMIT ?";
            $params[] = $limit;
            
            $stmt = $conexion->prepare($sql);
            $stmt->execute($params);
            
            return $stmt->fetchAll();
            
        } catch (PDOException $e) {
            error_log("Error en AccessLog::obtenerRecientes - " . $e->getMessage());
            return [];
        }
    }

    /**
     * Obtiene estadísticas de acciones por usuario
     * 
     * @param int $idUsuario ID del usuario
     * @return array Estadísticas agrupadas por acción
     */
    public static function obtenerEstadisticas($idUsuario)
    {
        try {
            $conexion = Database::getInstance()->getConexion();
            
            $stmt = $conexion->prepare("
                SELECT accion, COUNT(*) as cantidad, MAX(fecha) as ultima_vez
                FROM logs_acceso
                WHERE id_usuario = ?
                GROUP BY accion
                ORDER BY cantidad DESC
            ");
            
            $stmt->execute([$idUsuario]);
            return $stmt->fetchAll();
            
        } catch (PDOException $e) {
            error_log("Error en AccessLog::obtenerEstadisticas - " . $e->getMessage());
            return [];
        }
    }

    /**
     * Limpia logs antiguos de la base de datos (mantenimiento)
     * Se recomienda ejecutar periódicamente (cron job)
     * 
     * @param int $dias Días de antigüedad para eliminar
     * @return int Número de registros eliminados
     */
    public static function limpiarAntiguos($dias = 90)
    {
        try {
            $conexion = Database::getInstance()->getConexion();
            
            $stmt = $conexion->prepare("
                DELETE FROM logs_acceso
                WHERE fecha < DATE_SUB(NOW(), INTERVAL ? DAY)
            ");
            
            $stmt->execute([$dias]);
            return $stmt->rowCount();
            
        } catch (PDOException $e) {
            error_log("Error en AccessLog::limpiarAntiguos - " . $e->getMessage());
            return 0;
        }
    }

    /**
     * Obtiene la última acción de un tipo específico para un usuario
     * 
     * @param int $idUsuario ID del usuario
     * @param string $accion Tipo de acción
     * @return array|null Datos del log o null si no existe
     */
    public static function obtenerUltimaAccion($idUsuario, $accion)
    {
        try {
            $conexion = Database::getInstance()->getConexion();
            
            $stmt = $conexion->prepare("
                SELECT id_log, id_usuario, accion, ip, fecha
                FROM logs_acceso
                WHERE id_usuario = ? AND accion = ?
                ORDER BY fecha DESC
                LIMIT 1
            ");
            
            $stmt->execute([$idUsuario, $accion]);
            $result = $stmt->fetch();
            
            return $result ?: null;
            
        } catch (PDOException $e) {
            error_log("Error en AccessLog::obtenerUltimaAccion - " . $e->getMessage());
            return null;
        }
    }

    /**
     * Obtiene la dirección IP del cliente
     * Considera proxies y CDNs
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
}
