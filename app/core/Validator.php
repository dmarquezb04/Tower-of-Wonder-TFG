<?php
/**
 * Validator
 * Funciones de validación centralizadas
 * 
 * @package TowerOfWonder\Core
 * @author Darío Márquez Bautista
 */

/**
 * Valida y sanitiza un nombre de usuario
 * - Elimina caracteres no válidos (solo permite letras, números y guiones bajos)
 * - Recorta si excede la longitud máxima
 * - Valida longitud mínima
 * 
 * @param string $username Nombre de usuario a validar
 * @param int $min Longitud mínima (por defecto 2)
 * @param int $max Longitud máxima (por defecto 20)
 * @return array ['valid' => bool, 'username' => string, 'errors' => array]
 */
function validar_usuario($username, $min = 2, $max = 20) {
    $errors = [];
    $original = $username;
    
    // Eliminar espacios al inicio y final
    $username = trim($username);
    
    // Verificar si está vacío
    if ($username === '') {
        $errors[] = 'El nombre de usuario es obligatorio';
        return [
            'valid' => false,
            'username' => '',
            'errors' => $errors
        ];
    }
    
    // Eliminar caracteres no válidos (solo letras, números y guiones bajos)
    $username = preg_replace('/[^a-zA-Z0-9_]/', '', $username);
    
    // Si después de sanitizar queda vacío
    if ($username === '') {
        $errors[] = 'El nombre de usuario solo puede contener letras, números y guiones bajos';
        return [
            'valid' => false,
            'username' => '',
            'errors' => $errors
        ];
    }
    
    // Recortar si excede la longitud máxima
    if (strlen($username) > $max) {
        $username = substr($username, 0, $max);
    }
    
    // Validar longitud mínima
    if (strlen($username) < $min) {
        $errors[] = "El nombre de usuario debe tener al menos $min caracteres";
        return [
            'valid' => false,
            'username' => $username,
            'errors' => $errors
        ];
    }
    
    return [
        'valid' => true,
        'username' => $username,
        'errors' => []
    ];
}

/**
 * Valida que un campo no esté vacío
 * @param mixed $valor El valor a validar
 * @return bool true si tiene contenido, false si está vacío
 */
function validar_campo_requerido($valor) {
    return !empty(trim($valor));
}

/**
 * Valida formato de email
 * @param string $email Email a validar
 * @return bool true si es válido, false si no
 */
function validar_email($email) {
    return filter_var($email, FILTER_VALIDATE_EMAIL) !== false;
}


/**
 * Sanitiza una URL de referer para prevenir redirecciones externas
 * @param string|null $referer URL de referer
 * @param string $default URL por defecto si falla
 * @return string Ruta relativa segura
 */
function sanitizar_referer($referer, $default = '/') {
    if (empty($referer)) {
        return $default;
    }
    
    // Extraer solo el path (sin dominio)
    $path = parse_url($referer, PHP_URL_PATH);
    
    // Si no hay path válido, retornar default
    if (empty($path)) {
        return $default;
    }
    
    // Asegurarse de que empiece con /
    return $path[0] === '/' ? $path : '/' . $path;
}

/**
 * Valida contraseña segura con requisitos específicos
 * Verifica: longitud, mayúsculas, minúsculas, números y espacios
 * 
 * @param string $password Contraseña a validar
 * @param int $minLength Longitud mínima (por defecto 8)
 * @return array ['valid' => bool, 'errors' => array]
 */
function validar_password_segura($password, $minLength = 8) {
    $errors = [];

    // Longitud mínima
    if (strlen($password) < $minLength) {
        $errors[] = "La contraseña debe tener al menos $minLength caracteres";
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
