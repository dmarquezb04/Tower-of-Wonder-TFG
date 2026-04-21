<?php

/**
 * Endpoint de Login
 * Procesa el formulario de inicio de sesión
 * 
 * TEMPORAL: Este archivo será reemplazado por AuthController
 * cuando se implemente la capa de controllers completa
 */

// Cargar dependencias
require_once __DIR__ . '/../../config/config.php';
require_once __DIR__ . '/../../app/core/Validator.php';
require_once __DIR__ . '/../../app/services/AuthService.php';

// 1. Solo permitir POST
if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    $redirect_url = rtrim(BASE_URL, '/') . '/index.php';
    if (isset($_GET['error'])) {
        $redirect_url .= '?error=' . urlencode($_GET['error']) . '&modal=login';
    }
    header('Location: ' . $redirect_url);
    exit;
}

// 2. Obtener la página de origen
$referer = sanitizar_referer($_POST['referer'] ?? $_SERVER['HTTP_REFERER'] ?? rtrim(BASE_URL, '/') . '/index.php');

// 3. Recoger datos del formulario
$email = trim($_POST['email'] ?? '');
$password = $_POST['password'] ?? '';

// 4. Llamar al servicio de autenticación (SOLO lógica de negocio)
$resultado = AuthService::login($email, $password);

// 5. Procesar respuesta según el estado
switch ($resultado['status']) {

    case AuthService::LOGIN_OK:
        SessionHelper::login($resultado['user']);

        header('Location: ' . $referer);
        exit;

    case AuthService::LOGIN_2FA_REQUIRED:

        $_SESSION['2fa_user_id'] = $resultado['user']->id_usuario;
        $_SESSION['2fa_time'] = time();

        header('Location: verify_2fa.php');
        exit;

    case AuthService::LOGIN_BLOCKED:
        // Usuario bloqueado por demasiados intentos
        header('Location: ' . $referer . '?error=cuenta_bloqueada&modal=login&message=' . urlencode($resultado['message']));
        exit;

    case AuthService::LOGIN_INVALID_EMAIL:
        // Email con formato inválido
        header('Location: ' . $referer . '?error=email_invalido&modal=login');
        exit;

    case AuthService::LOGIN_INVALID_CREDENTIALS:
        // Credenciales incorrectas (email o contraseña)
        header('Location: ' . $referer . '?error=login_incorrecto&modal=login');
        exit;

    case AuthService::LOGIN_ERROR:
    default:
        // Error general del sistema
        header('Location: ' . $referer . '?error=error_sistema&modal=login');
        exit;
}
