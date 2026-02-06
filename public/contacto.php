<?php require_once __DIR__ . "/../config/config.php"; ?>
<?php require PARTIALS_PATH . "header.php"; ?>

<div id="contenido">
    <form action="mailto:dmarquezb04@educarex.es" method="post" id="form_contacto">
        <div>
            <legend>
                <h1>Formulario de contacto</h1>
            </legend>
        </div>

        <!-- Reestructurado: filas con label + campo para alinear horizontalmente -->
        <div class="form-row">
            <label for="nombre">Nombre:</label>
            <div class="form-field"><input type="text" id="nombre" name="nombre" required></div>
        </div>

        <div class="form-row">
            <label for="email">Email:</label>
            <div class="form-field"><input type="email" id="email" name="email" required></div>
        </div>

        <div class="form-row">
            <label for="asunto">Asunto:</label>
            <div class="form-field"><input type="text" id="asunto" name="asunto"></div>
        </div>

        <div class="form-row">
            <label for="mensaje">Mensaje:</label>
            <div class="form-field"><textarea id="mensaje" name="mensaje" rows="6" required></textarea></div>
        </div>

        <div class="form-row form-row--center">
            <div></div>
            <div class="form-field"><input type="submit" value="Enviar"></div>
        </div>
    </form>
</div>

<?php require PARTIALS_PATH . "footer.php"; ?>