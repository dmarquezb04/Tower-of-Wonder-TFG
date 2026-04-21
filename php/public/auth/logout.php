<?php
declare(strict_types=1);

// Cargar configuración y helper de sesión
require_once __DIR__ . '/../../config/config.php';
require_once __DIR__ . '/../../app/helpers/session.php';

// Cerrar sesión
SessionHelper::logout();

// Redirigir al inicio
header('Location: ' . BASE_URL . 'index.php');
exit;
