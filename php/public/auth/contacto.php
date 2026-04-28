<?php
/**
 * Endpoint de contacto — recibe JSON de React, envía email con PHPMailer via Gmail SMTP
 * POST /auth/contacto.php
 */
require_once __DIR__ . "/../../config/config.php";
require_once __DIR__ . "/../../vendor/autoload.php";

use PHPMailer\PHPMailer\PHPMailer;
use PHPMailer\PHPMailer\SMTP;
use PHPMailer\PHPMailer\Exception;

header('Content-Type: application/json; charset=utf-8');

// Solo POST
if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    http_response_code(405);
    echo json_encode(['ok' => false, 'message' => 'Método no permitido']);
    exit;
}

// Leer JSON del body
$raw = file_get_contents('php://input');
$data = json_decode($raw, true);

if (!$data) {
    http_response_code(400);
    echo json_encode(['ok' => false, 'message' => 'Datos inválidos']);
    exit;
}

// Validar campos obligatorios
$nombre  = trim($data['nombre']  ?? '');
$email   = trim($data['email']   ?? '');
$asunto  = trim($data['asunto']  ?? '(Sin asunto)');
$mensaje = trim($data['mensaje'] ?? '');

if (empty($nombre) || empty($email) || empty($mensaje)) {
    http_response_code(400);
    echo json_encode(['ok' => false, 'message' => 'Nombre, email y mensaje son obligatorios']);
    exit;
}

if (!filter_var($email, FILTER_VALIDATE_EMAIL)) {
    http_response_code(400);
    echo json_encode(['ok' => false, 'message' => 'El formato del email no es válido']);
    exit;
}

// Leer credenciales SMTP del entorno
$smtpHost = getenv('MAIL_HOST')      ?: 'smtp.gmail.com';
$smtpPort = (int)(getenv('MAIL_PORT') ?: 587);
$smtpUser = getenv('MAIL_USERNAME')  ?: '';
$smtpPass = getenv('MAIL_PASSWORD')  ?: '';
$mailFrom = getenv('MAIL_FROM')      ?: $smtpUser;
$mailFromName = getenv('MAIL_FROM_NAME') ?: SITE_NAME;
$mailTo   = getenv('MAIL_TO')        ?: $smtpUser;

if (empty($smtpUser) || empty($smtpPass)) {
    error_log('[Contacto] SMTP no configurado: MAIL_USERNAME o MAIL_PASSWORD vacíos');
    http_response_code(500);
    echo json_encode(['ok' => false, 'message' => 'El servidor de correo no está configurado. Inténtalo más tarde.']);
    exit;
}

// Enviar con PHPMailer
$mail = new PHPMailer(true);

try {
    // Servidor SMTP
    $mail->isSMTP();
    $mail->Host       = $smtpHost;
    $mail->SMTPAuth   = true;
    $mail->Username   = $smtpUser;
    $mail->Password   = $smtpPass;
    $mail->SMTPSecure = PHPMailer::ENCRYPTION_STARTTLS;
    $mail->Port       = $smtpPort;
    $mail->CharSet    = 'UTF-8';

    // Remitente y destinatario
    $mail->setFrom($mailFrom, $mailFromName);
    $mail->addAddress($mailTo);
    $mail->addReplyTo($email, $nombre); // Responder al usuario directamente

    // Contenido del email
    $mail->isHTML(true);
    $mail->Subject = "[Tower of Wonder] $asunto";
    $mail->Body    = "
        <div style='font-family: Arial, sans-serif; max-width: 600px;'>
            <h2 style='color: #5b4632;'>Nuevo mensaje de contacto</h2>
            <table style='width:100%; border-collapse: collapse;'>
                <tr>
                    <td style='padding: 8px; font-weight:bold; width:100px;'>Nombre:</td>
                    <td style='padding: 8px;'>" . htmlspecialchars($nombre) . "</td>
                </tr>
                <tr style='background:#f9f4ed;'>
                    <td style='padding: 8px; font-weight:bold;'>Email:</td>
                    <td style='padding: 8px;'>" . htmlspecialchars($email) . "</td>
                </tr>
                <tr>
                    <td style='padding: 8px; font-weight:bold;'>Asunto:</td>
                    <td style='padding: 8px;'>" . htmlspecialchars($asunto) . "</td>
                </tr>
                <tr style='background:#f9f4ed;'>
                    <td style='padding: 8px; font-weight:bold; vertical-align:top;'>Mensaje:</td>
                    <td style='padding: 8px;'>" . nl2br(htmlspecialchars($mensaje)) . "</td>
                </tr>
            </table>
            <hr style='border-color:#e0d0b8; margin-top:20px;'>
            <p style='color:#888; font-size:12px;'>Enviado desde el formulario de contacto de " . htmlspecialchars(SITE_NAME) . "</p>
        </div>
    ";
    $mail->AltBody = "Nombre: $nombre\nEmail: $email\nAsunto: $asunto\n\n$mensaje";

    $mail->send();
    echo json_encode(['ok' => true, 'message' => 'Mensaje enviado correctamente']);

} catch (Exception $e) {
    error_log('[Contacto] PHPMailer error: ' . $mail->ErrorInfo);
    http_response_code(500);
    echo json_encode(['ok' => false, 'message' => 'Error al enviar el mensaje. Inténtalo más tarde.']);
}
