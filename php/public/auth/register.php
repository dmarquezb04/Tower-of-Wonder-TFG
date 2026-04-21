<?php
// Cargar funciones de validación
require_once __DIR__ . '/../../app/core/Validator.php';

// 1. Solo permitir POST
if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    header('Location: /');
    exit;
}

// 2. Obtener la página de origen
$referer = sanitizar_referer($_POST['referer'] ?? $_SERVER['HTTP_REFERER'] ?? '/');

// 3. Recoger datos
$email = $_POST['email'] ?? '';
$password = $_POST['password'] ?? '';

// 4. Validación de campos requeridos
if (!validar_campo_requerido($email) || !validar_campo_requerido($password)) {
    header('Location: ' . $referer . '?error=campos_vacios&modal=register');
    exit;
}

// 5. Validar formato de email
if (!validar_email($email)) {
    header('Location: ' . $referer . '?error=email_invalido&modal=register');
    exit;
}

// 6. Validar contraseña segura
$validacion_password = validar_password_segura($password);
if (!$validacion_password['valid']) {
    header('Location: ' . $referer . '?error=password_invalida&modal=register');
    exit;
}

// 7. Cargar lógica de registro
require_once __DIR__ . '/../../app/services/RegisterService.php';

// 8. Intentar registrar
$resultado = RegisterService::registrar($email, $password);

// 9. Manejar resultado del registro
switch ($resultado['status']) {
    case RegisterService::REGISTER_OK:
        // Registro exitoso - redirigir a login
        header('Location: ' . $referer . '?success=registro_exitoso&modal=login');
        exit;
    
    case RegisterService::REGISTER_EMAIL_EXISTS:
        // El email ya está registrado
        header('Location: ' . $referer . '?error=email_existe&modal=register');
        exit;
    
    case RegisterService::REGISTER_ERROR:
    default:
        // Error en el registro
        header('Location: ' . $referer . '?error=error_servidor&modal=register');
        exit;
}
