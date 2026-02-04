<?php
function validar_usuario($username, $min=2, $max=20){
    $error="";
    if($username === ""){
        $error="NECESARIO INTRODUCIR USUARIO";
    }
    elseif(strlen($username) < $min || strlen($username) > $max){
        $error="EL USUARIO DEBE TENER ENTRE $min Y $max CARACTERES";
    }
    elseif(!preg_match("/^[a-zA-Z0-9_]+$/", $username)){
        $error="EL USUARIO SOLO PUEDE CONTENER LETRAS, NÚMEROS Y GUIONES BAJOS";
    }
    return $error === "" ? true : $error; 

}

function validar_password($password, $min=8, $max=20){
    $error="";
    if($password === ""){
        $error="NECESARIO INTRODUCIR CONTRASEÑA";
    }
    elseif(strlen($password) < $min || strlen($password) > $max){
        $error="LA CONTRASEÑA DEBE TENER ENTRE $min Y $max CARACTERES";
    }
    elseif(!preg_match('/^\S+$/', $password)){
        $error="LA CONTRASEÑA NO PUEDE CONTENER ESPACIOS";
    }
    return $error === "" ? true : $error; 
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
