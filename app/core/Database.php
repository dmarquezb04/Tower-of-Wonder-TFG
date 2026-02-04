<?php
/**
 * Clase Database - Gestor de conexión a base de datos
 * Implementa el patrón Singleton para una única instancia de conexión
 */
class Database
{
    private static $instance = null;
    private $conexion;

    /**
     * Constructor privado (patrón Singleton)
     */
    private function __construct()
    {
        // Cargar configuración
        require_once __DIR__ . '/../../config/config.php';

        $dsn = 'mysql:host=' . DB_HOST . ';dbname=' . DB_NAME . ';charset=utf8mb4';
        
        try {
            $this->conexion = new PDO($dsn, DB_USER, DB_PASS);
            
            // Configurar el modo de error para que lance excepciones
            $this->conexion->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);
            
            // Configurar el modo de fetch por defecto (array asociativo)
            $this->conexion->setAttribute(PDO::ATTR_DEFAULT_FETCH_MODE, PDO::FETCH_ASSOC);
            
            // Desactivar emulación de prepared statements para mayor seguridad
            $this->conexion->setAttribute(PDO::ATTR_EMULATE_PREPARES, false);
            
        } catch (PDOException $e) {
            // En producción, no mostrar detalles del error
            if (ENV === 'development') {
                die('Error de conexión a la base de datos: ' . $e->getMessage());
            } else {
                error_log('Error de conexión a la base de datos: ' . $e->getMessage());
                die('Error de conexión a la base de datos. Por favor, contacta al administrador.');
            }
        }
    }

    /**
     * Obtiene la instancia única de la clase (Singleton)
     * 
     * @return Database
     */
    public static function getInstance()
    {
        if (self::$instance === null) {
            self::$instance = new self();
        }
        return self::$instance;
    }

    /**
     * Obtiene la conexión PDO
     * 
     * @return PDO
     */
    public function getConexion()
    {
        return $this->conexion;
    }

    /**
     * Prevenir clonación del objeto (Singleton)
     */
    private function __clone() {}

    /**
     * Prevenir deserialización del objeto (Singleton)
     */
    public function __wakeup()
    {
        throw new Exception("No se puede deserializar un Singleton");
    }
}
