<?php
require_once __DIR__ . '/../models/Usuario.php';
require_once __DIR__ . '/../models/AccessLog.php';
require_once __DIR__ . '/../models/Role.php';

/**
 * Servicio de Registro de Usuarios
 * Gestiona SOLO la lógica de negocio del registro
 * NO maneja SQL directamente, NO maneja sesiones
 */
class RegisterService
{
    // Constantes de resultado
    const REGISTER_OK = 'OK';
    const REGISTER_EMAIL_EXISTS = 'EMAIL_EXISTS';
    const REGISTER_ERROR = 'ERROR';

    /**
     * Registra un nuevo usuario (SOLO LÓGICA DE NEGOCIO)
     * 
     * @param string $email Email del usuario
     * @param string $password Contraseña sin hashear
     * @return array ['status' => string, 'user' => Usuario|null, 'message' => string]
     */
    public static function registrar($email, $password)
    {
        try {
            // 1. Verificar si el email ya existe
            if (Usuario::emailExists($email)) {
                return [
                    'status' => self::REGISTER_EMAIL_EXISTS,
                    'user' => null,
                    'message' => 'El email ya está registrado'
                ];
            }
            
            // 2. Hash de la contraseña
            $passwordHash = password_hash($password, PASSWORD_BCRYPT);
            
            // 3. Crear usuario usando el modelo (retorna ID)
            $userId = Usuario::create($email, $passwordHash);
            
            if (!$userId) {
                return [
                    'status' => self::REGISTER_ERROR,
                    'user' => null,
                    'message' => 'Error al crear el usuario'
                ];
            }
            
            // 4. Cargar el usuario completo
            $usuario = Usuario::findById($userId);
            
            if (!$usuario) {
                return [
                    'status' => self::REGISTER_ERROR,
                    'user' => null,
                    'message' => 'Error al cargar los datos del usuario'
                ];
            }
            
            // 5. Asignar rol de usuario por defecto
            Role::asignarRol($userId, Role::ROLE_USER);
            
            // 6. Registrar la acción en logs de acceso
            AccessLog::registrar($userId, AccessLog::ACTION_REGISTER);
            
            return [
                'status' => self::REGISTER_OK,
                'user' => $usuario,
                'message' => 'Usuario registrado correctamente'
            ];
            
        } catch (Exception $e) {
            error_log("Error al registrar usuario: " . $e->getMessage());
            return [
                'status' => self::REGISTER_ERROR,
                'user' => null,
                'message' => 'Error interno del servidor'
            ];
        }
    }
    
    /**
     * Valida los requisitos de la contraseña
     * 
     * @param string $password
     * @return array ['valida' => bool, 'errores' => array]
     */
    public static function validarPassword($password)
    {
        $resultado = validar_password_segura($password);
        
        return [
            'valida' => $resultado['valid'],
            'errores' => $resultado['errors']
        ];
    }
}
