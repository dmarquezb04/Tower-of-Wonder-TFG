<?php 
require_once __DIR__ . "/../config/config.php";
require_once __DIR__ . "/../app/helpers/session.php";
?>

<!DOCTYPE html>
<html lang="es">

<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><?= SITE_NAME ?></title>
    <meta name="author" content="Darío Márquez Bautista">
    <link rel="stylesheet" href="<?= ASSETS_URL ?>css/estilos.css">
    <link rel="shortcut icon" href="<?= ASSETS_URL ?>favicon.ico" type="image/png">
    <!--Si la pagina es contacto.php, tiene también el css contacto.css -->
    <?php if (basename($_SERVER["PHP_SELF"]) === "contacto.php"): ?>
        <link rel="stylesheet" href="<?= ASSETS_URL ?>css/contacto.css">
    <?php endif; ?>
    <script>
        // Pasar BASE_URL de PHP a JavaScript
        const BASE_URL = '<?= BASE_URL ?>';
    </script>
    <script src="<?= ASSETS_URL ?>js/interfaz_login.js"></script>
</head>

<body>
    <header>
        <a href="<?= BASE_URL ?>index.php"><img src="<?= ASSETS_URL ?>img/logo.png" alt="Logo Tower of Wonder" id="logo"></a> <!--Logo-->
        
        <?php if (SessionHelper::isAuthenticated()): ?>
            <div id="user-welcome" style="cursor: pointer;">
                Bienvenido, <?= htmlspecialchars(SessionHelper::getUsername()) ?> | <a href="<?= BASE_URL ?>auth/logout.php" style="text-decoration: underline;">Salir</a>
            </div>
        <?php else: ?>
            <div id="login" onclick="abrirModalLogin()" style="cursor: pointer;">LOGIN</div>
        <?php endif; ?>
        
        <button id="menu-toggle" onclick="toggleMenu()" aria-label="Menú">☰</button>
        
        <div id="menudiv"> <!--Menú desplegable de la cabecera-->
            <nav>
                <ul id="menu">
                    <li><a href="<?= BASE_URL ?>index.php">INICIO</a></li>
                    <li>
                        <a href="#" onclick="toggleSubmenu(event)">PERSONAJES</a>
                        <ul>
                            <li><a href="#">Personaje 1</a></li>
                            <li><a href="#">Personaje 2</a></li>
                            <li><a href="#">Personaje 3</a></li>
                            <li><a href="#">Personaje 4</a></li>
                        </ul>
                    </li>
                    <li>
                        <a href="#" onclick="toggleSubmenu(event)">NOTICIAS</a>
                        <ul>
                            <li><a href="#">Updates</a></li>
                            <li><a href="#">Blog de desarrollo</a></li>
                        </ul>
                    </li>
                    <li><a href="#">FAQ</a></li>
                    <li><a href="<?= BASE_URL ?>contacto.php">CONTACTO</a></li>
                </ul>
            </nav>
        </div>
    </header>