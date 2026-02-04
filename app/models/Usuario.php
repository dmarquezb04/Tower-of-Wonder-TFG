<?php
/**
 * Modelo Usuario
 * Gestiona el acceso a datos de la tabla usuarios
 * 
 * @package TowerOfWonder\Models
 * @author Darío Márquez Bautista
 */

require_once __DIR__ . '/../core/Database.php';

class Usuario
{
    // Propiedades del usuario
    public $id_usuario;
    public $email;
    public $username;
    public $password_hash;
    public $two_fa_enabled;
    public $activo;
    public $fecha_creacion;
    public $ultimo_login;

    /**
     * Constructor privado para evitar instanciación directa
     * Usar métodos estáticos para obtener instancias
     */
    private function __construct() {}

    /**
     * Busca un usuario por su email
     * 
     * @param string $email Email del usuario a buscar
     * @return Usuario|null Instancia de Usuario o null si no existe
     */
    public static function findByEmail($email)
    {
        try {
            $conexion = Database::getInstance()->getConexion();
            
            $stmt = $conexion->prepare("
                SELECT id_usuario, email, username, password_hash, two_fa_enabled, 
                       activo, fecha_creacion, ultimo_login
                FROM usuarios 
                WHERE email = ? AND activo = 1
            ");
            
            $stmt->execute([$email]);
            $data = $stmt->fetch();
            
            if (!$data) {
                return null;
            }
            
            return self::hydrate($data);
            
        } catch (PDOException $e) {
            error_log("Error en Usuario::findByEmail - " . $e->getMessage());
            return null;
        }
    }

    /**
     * Busca un usuario por su ID
     * 
     * @param int $id ID del usuario
     * @return Usuario|null Instancia de Usuario o null si no existe
     */
    public static function findById($id)
    {
        try {
            $conexion = Database::getInstance()->getConexion();
            
            $stmt = $conexion->prepare("
                SELECT id_usuario, email, username, password_hash, two_fa_enabled, 
                       activo, fecha_creacion, ultimo_login
                FROM usuarios 
                WHERE id_usuario = ? AND activo = 1
            ");
            
            $stmt->execute([$id]);
            $data = $stmt->fetch();
            
            if (!$data) {
                return null;
            }
            
            return self::hydrate($data);
            
        } catch (PDOException $e) {
            error_log("Error en Usuario::findById - " . $e->getMessage());
            return null;
        }
    }

    /**
     * Crea un nuevo usuario en la base de datos
     * 
     * @param string $email Email del usuario
     * @param string $passwordHash Hash de la contraseña
     * @return int|false ID del usuario creado o false si falla
     */
    public static function create($email, $passwordHash)
    {
        try {
            $conexion = Database::getInstance()->getConexion();
            
            // Generar username desde el email (parte antes de la @)
            $usernameBase = explode('@', $email)[0];
            
            // Validar y sanitizar el username
            require_once __DIR__ . '/../core/Validator.php';
            $validacion = validar_usuario($usernameBase);
            
            // Si no es válido, usar un username genérico
            $username = $validacion['valid'] ? $validacion['username'] : 'user_' . time();
            
            $stmt = $conexion->prepare("
                INSERT INTO usuarios (email, username, password_hash, activo, fecha_creacion)
                VALUES (?, ?, ?, 1, NOW())
            ");
            
            $stmt->execute([$email, $username, $passwordHash]);
            
            return (int) $conexion->lastInsertId();
            
        } catch (PDOException $e) {
            error_log("Error en Usuario::create - " . $e->getMessage());
            return false;
        }
    }

    /**
     * Actualiza la fecha del último login
     * 
     * @param int $userId ID del usuario
     * @return bool true si se actualiza correctamente
     */
    public static function updateLastLogin($userId)
    {
        try {
            $conexion = Database::getInstance()->getConexion();
            
            $stmt = $conexion->prepare("
                UPDATE usuarios 
                SET ultimo_login = NOW() 
                WHERE id_usuario = ?
            ");
            
            return $stmt->execute([$userId]);
            
        } catch (PDOException $e) {
            error_log("Error en Usuario::updateLastLogin - " . $e->getMessage());
            return false;
        }
    }

    /**
     * Actualiza el nombre de usuario
     * Valida y sanitiza automáticamente el username
     * 
     * @param int $userId ID del usuario
     * @param string $newUsername Nuevo nombre de usuario
     * @return array ['success' => bool, 'username' => string, 'errors' => array]
     */
    public static function updateUsername($userId, $newUsername)
    {
        try {
            require_once __DIR__ . '/../core/Validator.php';
            
            // Validar y sanitizar el username
            $validacion = validar_usuario($newUsername);
            
            if (!$validacion['valid']) {
                return [
                    'success' => false,
                    'username' => $validacion['username'],
                    'errors' => $validacion['errors']
                ];
            }
            
            $conexion = Database::getInstance()->getConexion();
            
            // Verificar si el username ya existe
            $stmt = $conexion->prepare("
                SELECT COUNT(*) as count
                FROM usuarios
                WHERE username = ? AND id_usuario != ?
            ");
            $stmt->execute([$validacion['username'], $userId]);
            $result = $stmt->fetch();
            
            if ($result['count'] > 0) {
                return [
                    'success' => false,
                    'username' => $validacion['username'],
                    'errors' => ['El nombre de usuario ya está en uso']
                ];
            }
            
            // Actualizar el username
            $stmt = $conexion->prepare("
                UPDATE usuarios 
                SET username = ? 
                WHERE id_usuario = ?
            ");
            
            $success = $stmt->execute([$validacion['username'], $userId]);
            
            return [
                'success' => $success,
                'username' => $validacion['username'],
                'errors' => []
            ];
            
        } catch (PDOException $e) {
            error_log("Error en Usuario::updateUsername - " . $e->getMessage());
            return [
                'success' => false,
                'username' => '',
                'errors' => ['Error al actualizar el nombre de usuario']
            ];
        }
    }

    /**
     * Verifica si un email ya existe en la base de datos
     * 
     * @param string $email Email a verificar
     * @return bool true si existe, false si no
     */
    public static function emailExists($email)
    {
        try {
            $conexion = Database::getInstance()->getConexion();
            
            $stmt = $conexion->prepare("
                SELECT COUNT(*) as count 
                FROM usuarios 
                WHERE email = ?
            ");
            
            $stmt->execute([$email]);
            $result = $stmt->fetch();
            
            return $result['count'] > 0;
            
        } catch (PDOException $e) {
            error_log("Error en Usuario::emailExists - " . $e->getMessage());
            return false;
        }
    }

    /**
     * Hidrata un objeto Usuario con datos del array
     * 
     * @param array $data Datos del usuario desde la BD
     * @return Usuario Instancia hidratada
     */
    private static function hydrate($data)
    {
        $usuario = new self();
        $usuario->id_usuario = $data['id_usuario'];
        $usuario->email = $data['email'];
        $usuario->username = $data['username'] ?? null;
        $usuario->password_hash = $data['password_hash'];
        $usuario->two_fa_enabled = (bool) $data['two_fa_enabled'];
        $usuario->activo = (bool) $data['activo'];
        $usuario->fecha_creacion = $data['fecha_creacion'];
        $usuario->ultimo_login = $data['ultimo_login'] ?? null;
        
        return $usuario;
    }

    /**
     * Convierte el usuario a array (sin password_hash por seguridad)
     * 
     * @return array
     */
    public function toArray()
    {
        return [
            'id_usuario' => $this->id_usuario,
            'email' => $this->email,
            'two_fa_enabled' => $this->two_fa_enabled,
            'activo' => $this->activo,
            'fecha_creacion' => $this->fecha_creacion,
            'ultimo_login' => $this->ultimo_login
        ];
    }
}
