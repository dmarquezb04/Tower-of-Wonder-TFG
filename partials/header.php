<?php require_once __DIR__ . "/../config/config.php"; ?>

<!DOCTYPE html>
<html lang="es">

<head>
    <meta charset="UTF-8">
    <title><?= SITE_NAME ?></title>
    <meta name="author" content="Darío Márquez Bautista">
    <link rel="stylesheet" href="<?= ASSETS_URL ?>css/estilos.css">
    <!--Si la pagina es contacto.php, tiene también el css contacto.css -->
    <?php if (basename($_SERVER["PHP_SELF"]) === "contacto.php"): ?>
        <link rel="stylesheet" href="<?= ASSETS_URL ?>css/contacto.css">
    <?php endif; ?>
    <script src="<?= ASSETS_URL ?>js/interfaz_login.js"></script>
</head>

<body>
    <header>
        <a href="<?= BASE_URL ?>index.php"><img src="<?= ASSETS_URL ?>img/logo.png" alt="Logo Tower of Wonder" id="logo"></a> <!--Logo-->
        <div id="menudiv"> <!--Menú desplegable de la cabecera-->
            <nav>
                <ul id="menu">
                    <li><a href="<?= BASE_URL ?>index.php">INICIO</a></li>
                    <li>
                        <a href="#">PERSONAJES</a>
                        <ul>
                            <li><a href="#">Personaje 1</a></li>
                            <li><a href="#">Personaje 2</a></li>
                            <li><a href="#">Personaje 3</a></li>
                            <li><a href="#">Personaje 4</a></li>
                        </ul>
                    </li>
                    <li>
                        <a href="#">NOTICIAS</a>
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
        <div id="login">
            <span onclick="abrirModalLogin()" style="cursor: pointer;">LOGIN</span>
            <div id="link_descarga">
                &darr; <!--Icono descarga-->
            </div>
        </div>
    </header>