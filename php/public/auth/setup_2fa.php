<?php
require_once "../../config/config.php";
require_once "../../app/lib/GoogleAuthenticator.php";

// Debe estar logueado
if (!isset($_SESSION['user_id'])) {
    header("Location: login.php");
    exit;
}

$ga = new PHPGangsta_GoogleAuthenticator();

// Generar secret temporal
if (!isset($_SESSION['2fa_secret'])) {
    $_SESSION['2fa_secret'] = $ga->createSecret();
}

$secret = $_SESSION['2fa_secret'];

// Obtener username real desde BD
$user_id = $_SESSION['user_id'];

require_once "../../app/models/Usuario.php";

$user = Usuario::findById($user_id);

if (!$user) {
    header("Location: login.php");
    exit;
}

$username = $user->username;

$qrCodeUrl = $ga->getQRCodeGoogleUrl(
    'TowerOfWonder:' . $username,
    $secret
);
?>

<h2>Activar 2FA</h2>

<img src="<?= $qrCodeUrl ?>">

<form method="POST">
    <input type="text" name="code" placeholder="Código de 6 dígitos">
    <button type="submit">Confirmar</button>
</form>

<?php
if ($_SERVER['REQUEST_METHOD'] === 'POST') {

    $code = $_POST['code'];

    if ($ga->verifyCode($secret, $code, 2)) {

        // Guardar en BD usando el Modelo
        if (Usuario::enable2FA($user_id, $secret)) {
            unset($_SESSION['2fa_secret']);
            echo "2FA activado correctamente";
            // Pista: Aquí podrías redirigir al dashboard: header('Location: ../index.php');
        } else {
            echo "Error de base de datos interno.";
        }

        echo "2FA activado correctamente";
    } else {
        echo "Código incorrecto";
    }
}
