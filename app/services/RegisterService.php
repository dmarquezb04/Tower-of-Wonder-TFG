<?php
require_once __DIR__ . '/../models/Usuario.php';

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
            
            // 3. Crear usuario usando el modelo
            $usuario = Usuario::create($email, $passwordHash);
            
            if (!$usuario) {
                return [
                    'status' => self::REGISTER_ERROR,
                    'user' => null,
                    'message' => 'Error al crear el usuario'
                ];
            }
            
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
        $errores = [];
        
        if (strlen($password) < 8) {
            $errores[] = 'La contraseña debe tener al menos 8 caracteres';
        }
        
        if (!preg_match('/[A-Z]/', $password)) {
            $errores[] = 'Debe contener al menos una mayúscula';
        }
        
        if (!preg_match('/[a-z]/', $password)) {
            $errores[] = 'Debe contener al menos una minúscula';
        }
        
        if (!preg_match('/[0-9]/', $password)) {
            $errores[] = 'Debe contener al menos un número';
        }
        
        return [
            'valida' => empty($errores),
            'errores' => $errores
        ];
    }
}
