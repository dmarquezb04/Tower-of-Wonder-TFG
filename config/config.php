<?php
/**********************************************************
 * CONFIGURACIÓN GLOBAL DEL PROYECTO
 * TFG – Config.php
 **********************************************************/

/* =========================
   ENTORNO
   ========================= */
define("ENV", "development"); // development | production

if (ENV === "development") {
    error_reporting(E_ALL);
    ini_set("display_errors", 1);
} else {
    error_reporting(0);
    ini_set("display_errors", 0);
}

/* =========================
   RUTAS DEL PROYECTO
   ========================= */

// Ruta absoluta al directorio raíz del proyecto
define("ROOT_PATH", dirname(__DIR__) . "/");

// Rutas internas (servidor)
define("CONFIG_PATH", ROOT_PATH . "config/");
define("PARTIALS_PATH", ROOT_PATH . "partials/");
define("PUBLIC_PATH", ROOT_PATH . "public/");

// Rutas públicas (URL)
define("BASE_URL", "/GITHUB_PROYECTO/Tower-of-Wonder-TFG/public/"); // ajusta si el proyecto no está en la raíz // CAMBIAR AL MIGRAR
define("ASSETS_URL", BASE_URL . "assets/");

/* =========================
   DATOS GENERALES DEL SITIO
   ========================= */
define("SITE_NAME", "Tower of Wonder");
define("SITE_AUTHOR", "Darío Márquez Bautista");
define("SITE_LANG", "es");
//define("CURRENT_YEAR", date("Y"));

/* =========================
   ZONA HORARIA
   ========================= */
date_default_timezone_set("Europe/Madrid");

/* =========================
   BASE DE DATOS
   ========================= */
define("DB_HOST", "127.0.0.1");
define("DB_NAME", "tower_of_wonder");
define("DB_USER", "admin_tow_bbdd");
define("DB_PASS", "7kr3L1UBDWgcvgY");

/* =========================
   SEGURIDAD BÁSICA
   ========================= */
// Evita acceso directo al archivo desde el navegador
if (php_sapi_name() !== "cli" && basename($_SERVER["PHP_SELF"]) === basename(__FILE__)) {
    http_response_code(403);
    exit("Acceso denegado");
}
