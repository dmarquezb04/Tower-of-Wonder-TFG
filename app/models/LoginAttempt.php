<?php
/**
 * Modelo LoginAttempt
 * Gestiona los intentos de login para prevenir ataques de fuerza bruta
 * 
 * @package TowerOfWonder\Models
 * @author Darío Márquez Bautista
 */

require_once __DIR__ . '/../core/Database.php';

class LoginAttempt
{
    // Configuración de seguridad
    const MAX_ATTEMPTS = 5;              // Máximo de intentos fallidos
    const LOCKOUT_TIME = 900;            // Tiempo de bloqueo en segundos (15 minutos)
    const ATTEMPT_WINDOW = 900;          // Ventana de tiempo para contar intentos (15 minutos)

    /**
     * Registra un intento de login
     * 
     * @param string $email Email del intento
     * @param string $ip Dirección IP
     * @param bool $exitoso Si el intento fue exitoso
     * @return bool true si se registró correctamente
     */
    public static function registrar($email, $ip, $exitoso)
    {
        try {
            $conexion = Database::getInstance()->getConexion();
            
            $stmt = $conexion->prepare("
                INSERT INTO login_attempts (email, ip, exitoso, fecha)
                VALUES (?, ?, ?, NOW())
            ");
            
            return $stmt->execute([$email, $ip, $exitoso ? 1 : 0]);
            
        } catch (PDOException $e) {
            error_log("Error en LoginAttempt::registrar - " . $e->getMessage());
            return false;
        }
    }

    /**
     * Verifica si una IP o email está bloqueado por demasiados intentos fallidos
     * 
     * @param string $email Email a verificar
     * @param string $ip IP a verificar
     * @return array ['bloqueado' => bool, 'intentos' => int, 'tiempo_restante' => int]
     */
    public static function estaBloqueado($email, $ip)
    {
        try {
            $conexion = Database::getInstance()->getConexion();
            
            // Obtener intentos fallidos recientes (últimos 15 minutos)
            $stmt = $conexion->prepare("
                SELECT COUNT(*) as intentos, MAX(fecha) as ultimo_intento
                FROM login_attempts
                WHERE (email = ? OR ip = ?)
                  AND exitoso = 0
                  AND fecha > DATE_SUB(NOW(), INTERVAL ? SECOND)
            ");
            
            $stmt->execute([$email, $ip, self::ATTEMPT_WINDOW]);
            $result = $stmt->fetch();
            
            $intentos = $result['intentos'];
            $bloqueado = $intentos >= self::MAX_ATTEMPTS;
            
            // Calcular tiempo restante de bloqueo
            $tiempoRestante = 0;
            if ($bloqueado && $result['ultimo_intento']) {
                $ultimoIntento = strtotime($result['ultimo_intento']);
                $tiempoTranscurrido = time() - $ultimoIntento;
                $tiempoRestante = max(0, self::LOCKOUT_TIME - $tiempoTranscurrido);
            }
            
            return [
                'bloqueado' => $bloqueado,
                'intentos' => (int) $intentos,
                'tiempo_restante' => $tiempoRestante
            ];
            
        } catch (PDOException $e) {
            error_log("Error en LoginAttempt::estaBloqueado - " . $e->getMessage());
            // En caso de error, mejor no bloquear (fail-open para disponibilidad)
            return [
                'bloqueado' => false,
                'intentos' => 0,
                'tiempo_restante' => 0
            ];
        }
    }

    /**
     * Limpia intentos exitosos antiguos (después de un login correcto)
     * Esto permite que después de un login exitoso se resetee el contador
     * 
     * @param string $email Email del usuario
     * @param string $ip IP del usuario
     * @return bool
     */
    public static function limpiarIntentos($email, $ip)
    {
        try {
            $conexion = Database::getInstance()->getConexion();
            
            // Marcar como exitosos los intentos fallidos antiguos
            // (alternativa: borrarlos directamente)
            $stmt = $conexion->prepare("
                DELETE FROM login_attempts
                WHERE (email = ? OR ip = ?)
                  AND exitoso = 0
                  AND fecha > DATE_SUB(NOW(), INTERVAL ? SECOND)
            ");
            
            return $stmt->execute([$email, $ip, self::ATTEMPT_WINDOW]);
            
        } catch (PDOException $e) {
            error_log("Error en LoginAttempt::limpiarIntentos - " . $e->getMessage());
            return false;
        }
    }

    /**
     * Obtiene el historial de intentos de un usuario/IP
     * 
     * @param string $email Email (opcional)
     * @param string $ip IP (opcional)
     * @param int $limit Límite de resultados
     * @return array Lista de intentos
     */
    public static function obtenerHistorial($email = null, $ip = null, $limit = 50)
    {
        try {
            $conexion = Database::getInstance()->getConexion();
            
            $condiciones = [];
            $params = [];
            
            if ($email !== null) {
                $condiciones[] = "email = ?";
                $params[] = $email;
            }
            
            if ($ip !== null) {
                $condiciones[] = "ip = ?";
                $params[] = $ip;
            }
            
            $where = !empty($condiciones) ? "WHERE " . implode(" OR ", $condiciones) : "";
            
            $stmt = $conexion->prepare("
                SELECT id_intento, email, ip, exitoso, fecha
                FROM login_attempts
                $where
                ORDER BY fecha DESC
                LIMIT ?
            ");
            
            $params[] = $limit;
            $stmt->execute($params);
            
            return $stmt->fetchAll();
            
        } catch (PDOException $e) {
            error_log("Error en LoginAttempt::obtenerHistorial - " . $e->getMessage());
            return [];
        }
    }

    /**
     * Limpia intentos antiguos de la base de datos (mantenimiento)
     * Se recomienda ejecutar periódicamente (cron job)
     * 
     * @param int $dias Días de antigüedad para limpiar
     * @return int Número de registros eliminados
     */
    public static function limpiarAntiguos($dias = 30)
    {
        try {
            $conexion = Database::getInstance()->getConexion();
            
            $stmt = $conexion->prepare("
                DELETE FROM login_attempts
                WHERE fecha < DATE_SUB(NOW(), INTERVAL ? DAY)
            ");
            
            $stmt->execute([$dias]);
            return $stmt->rowCount();
            
        } catch (PDOException $e) {
            error_log("Error en LoginAttempt::limpiarAntiguos - " . $e->getMessage());
            return 0;
        }
    }
}
