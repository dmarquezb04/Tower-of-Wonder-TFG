<?php require_once __DIR__ . "/../config/config.php"; ?>

<footer>
    <div id="redes_sociales">
        <a href="https://www.x.com"><img src="<?= ASSETS_URL ?>img/social_media/twitter.png" alt="Twitter"></a>
        <a href="https://www.instagram.com"><img src="<?= ASSETS_URL ?>img/social_media/instagram.png" alt="Instagram"></a>
        <a href="https://www.youtube.com"><img src="<?= ASSETS_URL ?>img/social_media/youtube.png" alt="YouTube"></a>
        <a href="https://www.tiktok.com"><img src="<?= ASSETS_URL ?>img/social_media/tiktok.png" alt="TikTok"></a>
        <a href="https://www.discord.com"><img src="<?= ASSETS_URL ?>img/social_media/discord.png" alt="Discord"></a>
    </div>


    <div class="license desktop-only">
        <p class="license-text">
            © <?= CURRENT_YEAR ?> <?= SITE_AUTHOR ?> —
            Contenido bajo licencia
            <a href="<?= CC_LICENSE_URL ?>"
                target="_blank" rel="noopener noreferrer">
                <?= CC_LICENSE_NAME ?>
            </a>
        </p>
    </div>



    <div id="susc_newsletter">
        <form>
            <input type="email" placeholder="¡Suscríbete a nuestra newsletter!">
            <button type="submit">SUSCRIBIRSE</button>
        </form>
    </div>
</footer>
</body>

</html>