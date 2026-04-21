<?php
require_once "../../config/config.php";
require_once "../../app/lib/GoogleAuthenticator.php";
require_once "../../app/helpers/session.php";

SessionHelper::start();

if (!isset($_SESSION['2fa_user_id'])) {
    header("Location: login.php");
    exit;
}

// Expiración (5 min)
if (time() - $_SESSION['2fa_time'] > 300) {
    session_destroy();
    header("Location: login.php");
    exit;
}

$ga = new PHPGangsta_GoogleAuthenticator();

$user_id = $_SESSION['2fa_user_id'];

require_once "../../app/models/Usuario.php";

$user = Usuario::findById($user_id);

if (empty($user->twofa_secret)) {
    session_destroy();
    header("Location: login.php?error=2fa_not_configured");
    exit;
}

$secret = $user->twofa_secret;

if ($_SERVER['REQUEST_METHOD'] === 'POST') {

    $code = $_POST['code'];

    if ($ga->verifyCode($secret, $code, 2)) {

        require_once "../../app/helpers/session.php";

        // Login completo centralizado
        SessionHelper::login($user);
        SessionHelper::mark2FAVerified();

        unset($_SESSION['2fa_user_id']);
        unset($_SESSION['2fa_time']);

        header("Location: ../index.php");
        exit;
    } else {
        $error = "Código incorrecto";
    }
}
?>

<h2>Verificación 2FA</h2>

<form method="POST">
    <input type="text" name="code" placeholder="Código">
    <button type="submit">Verificar</button>
</form>

<?php if (isset($error)) echo $error; ?>