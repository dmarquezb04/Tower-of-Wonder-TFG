<?php require_once __DIR__ . "/../config/config.php"; ?>
<!DOCTYPE html>
<html lang="es">

<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><?= htmlspecialchars(SITE_NAME) ?></title>
    <meta name="description" content="Tower of Wonder – videojuego de fantasía y aventura. Descárgalo en Steam, PlayStation, Xbox y Nintendo Switch.">
    <meta name="author" content="<?= htmlspecialchars(SITE_AUTHOR) ?>">
    <link rel="shortcut icon" href="<?= ASSETS_URL ?>favicon.ico" type="image/x-icon">

    <!-- Bundle React generado por Vite -->
    <link rel="stylesheet" href="<?= ASSETS_URL ?>dist/main.css?v=<?= time() ?>">
</head>

<body>
    <!-- Datos de sesión PHP → React -->
    <script>
        window.APP_DATA = {
            baseUrl: '<?= BASE_URL ?>',
            assetsUrl: '<?= ASSETS_URL ?>',
            isAuthenticated: <?= SessionHelper::isAuthenticated() ? 'true' : 'false' ?>,
            username: '<?= htmlspecialchars(SessionHelper::getUsername() ?? '') ?>',
            currentPage: '<?= htmlspecialchars(basename($_SERVER['PHP_SELF'])) ?>',
        };
    </script>

    <!-- React monta todo el UI aquí -->
    <div id="root"></div>

    <script type="module" src="<?= ASSETS_URL ?>dist/main.js?v=<?= time() ?>"></script>