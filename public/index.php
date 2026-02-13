<?php require_once __DIR__ . "/../config/config.php"; ?>
<?php require PARTIALS_PATH . "header.php"; ?>

<div id="contenido">
    <div id="contenido_principal">
        <h1>¡Bienvenido a Tower of Wonder!</h1>
        <p>Sumérgete en un mundo de fantasía y aventura con nuestro nuevo videojuego. Explora paisajes impresionantes, enfrenta desafíos épicos y descubre secretos ocultos en cada rincón. ¡Únete a la comunidad de jugadores y vive la experiencia de Tower of Wonder hoy mismo!</p>
    </div>
    <div id="plataformas_descarga">
        <h2>⇓Descarga en todas las plataformas⇓</h2>
        <div id="plataformas">
            <a href="#" class="plataforma">
                <img src="<?php echo ASSETS_URL; ?>img/plataformas_descarga/steam_logo.png" alt="Steam">
            </a>
            <a href="#" class="plataforma">
                <img src="<?php echo ASSETS_URL; ?>img/plataformas_descarga/xbox_logo.png" alt="Xbox Series S/X">
            </a>
            <a href="#" class="plataforma">
                <img src="<?php echo ASSETS_URL; ?>img/plataformas_descarga/ps5_logo.png" alt="PlayStation 5">
            </a>
            <a href="#" class="plataforma">
                <img src="<?php echo ASSETS_URL; ?>img/plataformas_descarga/switch_logo.png" alt="Nintendo Switch">
            </a>
        </div>
    </div>
</div>

<?php require PARTIALS_PATH . "footer.php"; ?>