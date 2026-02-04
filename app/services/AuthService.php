<?php
/**
 * AuthService
 * Servicio de autenticación - SOLO lógica de negocio
 * 
 * Responsabilidades:
 * - Validar credenciales de login
 * - Verificar contraseñas
 * - Determinar si requiere 2FA
 * 
 * NO gestiona:
 * - Sesiones (responsabilidad de SessionHelper)
 * - Redirecciones (responsabilidad de Controllers)
 * - Acceso directo a BD (responsabilidad de Models)
 * 
 * @package TowerOfWonder\Services
 * @author Darío Márquez Bautista
 */

require_once __DIR__ . '/../core/Validator.php';
require_once __DIR__ . '/../models/Usuario.php';

class AuthService
{
    // Constantes de estado de autenticación
    const LOGIN_OK = 'LOGIN_OK';
    const LOGIN_2FA_REQUIRED = 'LOGIN_2FA_REQUIRED';
    const LOGIN_INVALID_CREDENTIALS = 'LOGIN_INVALID_CREDENTIALS';
    const LOGIN_INVALID_EMAIL = 'LOGIN_INVALID_EMAIL';
    const LOGIN_INVALID_PASSWORD = 'LOGIN_INVALID_PASSWORD';
    const LOGIN_ERROR = 'LOGIN_ERROR';

    /**
     * Autentica un usuario con email y contraseña
     * 
     * Proceso:
     * 1. Valida formato de email y contraseña
     * 2. Busca el usuario en la base de datos
     * 3. Verifica la contraseña con password_verify
     * 4. Determina si requiere verificación 2FA
     * 
     * @param string $email Email del usuario
     * @param string $password Contraseña en texto plano
     * @return array ['status' => string, 'user' => Usuario|null, 'message' => string]
     */
    public static function login($email, $password)
    {
        try {
            // 1. Validar que los campos no estén vacíos
            if (!validar_campo_requerido($email) || !validar_campo_requerido($password)) {
                return [
                    'status' => self::LOGIN_INVALID_CREDENTIALS,
                    'user' => null,
                    'message' => 'Email y contraseña son requeridos'
                ];
            }

            // 2. Validar formato de email
            if (!validar_email($email)) {
                return [
                    'status' => self::LOGIN_INVALID_EMAIL,
                    'user' => null,
                    'message' => 'El formato del email no es válido'
                ];
            }

            // 3. Buscar usuario por email en la base de datos
            $usuario = Usuario::findByEmail($email);

            // 4. Si no existe el usuario, retornar error genérico
            // (no revelamos si el email existe o no por seguridad)
            if (!$usuario) {
                return [
                    'status' => self::LOGIN_INVALID_CREDENTIALS,
                    'user' => null,
                    'message' => 'Credenciales incorrectas'
                ];
            }

            // 5. Verificar la contraseña usando password_verify
            if (!password_verify($password, $usuario->password_hash)) {
                return [
                    'status' => self::LOGIN_INVALID_CREDENTIALS,
                    'user' => null,
                    'message' => 'Credenciales incorrectas'
                ];
            }

            // 6. Credenciales válidas - Verificar si requiere 2FA
            if ($usuario->two_fa_enabled) {
                // Usuario tiene 2FA activado, requiere verificación adicional
                return [
                    'status' => self::LOGIN_2FA_REQUIRED,
                    'user' => $usuario,
                    'message' => 'Verificación 2FA requerida'
                ];
            }

            // 7. Login exitoso sin 2FA
            return [
                'status' => self::LOGIN_OK,
                'user' => $usuario,
                'message' => 'Login exitoso'
            ];

        } catch (Exception $e) {
            // Log del error (no exponer detalles al usuario)
            error_log("Error en AuthService::login - " . $e->getMessage());
            
            return [
                'status' => self::LOGIN_ERROR,
                'user' => null,
                'message' => 'Error al procesar el login'
            ];
        }
    }

    /**
     * Verifica un código 2FA
     * (Preparado para implementación futura)
     * 
     * @param int $userId ID del usuario
     * @param string $code Código 2FA proporcionado
     * @return array ['status' => bool, 'message' => string]
     */
    public static function verify2FACode($userId, $code)
    {
        // TODO: Implementar verificación de código 2FA
        // - Buscar código en tabla two_factor_codes
        // - Verificar que no haya expirado
        // - Verificar que coincida con el hash
        // - Marcar como usado
        
        return [
            'status' => false,
            'message' => 'Verificación 2FA no implementada aún'
        ];
    }

    /**
     * Verifica si una contraseña es válida según los requisitos
     * Útil para registro y cambio de contraseña
     * 
     * @param string $password Contraseña a validar
     * @return array ['valid' => bool, 'errors' => array]
     */
    public static function validatePassword($password)
    {
        $errors = [];

        // Longitud mínima
        if (!validar_password_longitud($password, 8)) {
            $errors[] = 'La contraseña debe tener al menos 8 caracteres';
        }

        // Al menos una mayúscula
        if (!preg_match('/[A-Z]/', $password)) {
            $errors[] = 'Debe contener al menos una letra mayúscula';
        }

        // Al menos una minúscula
        if (!preg_match('/[a-z]/', $password)) {
            $errors[] = 'Debe contener al menos una letra minúscula';
        }

        // Al menos un número
        if (!preg_match('/[0-9]/', $password)) {
            $errors[] = 'Debe contener al menos un número';
        }

        // Sin espacios
        if (preg_match('/\s/', $password)) {
            $errors[] = 'No puede contener espacios';
        }

        return [
            'valid' => empty($errors),
            'errors' => $errors
        ];
    }
}

