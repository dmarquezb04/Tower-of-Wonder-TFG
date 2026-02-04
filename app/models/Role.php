<?php
/**
 * Modelo Role
 * Gestiona roles y permisos de usuarios
 * 
 * @package TowerOfWonder\Models
 * @author Darío Márquez Bautista
 */

require_once __DIR__ . '/../core/Database.php';

class Role
{
    // Roles predefinidos
    const ROLE_ADMIN = 'admin';
    const ROLE_MODERATOR = 'moderator';
    const ROLE_USER = 'user';

    /**
     * Obtiene todos los roles disponibles
     * 
     * @return array Lista de roles
     */
    public static function obtenerTodos()
    {
        try {
            $conexion = Database::getInstance()->getConexion();
            
            $stmt = $conexion->prepare("
                SELECT id_rol, nombre_rol, descripcion
                FROM roles
                ORDER BY id_rol
            ");
            
            $stmt->execute();
            return $stmt->fetchAll();
            
        } catch (PDOException $e) {
            error_log("Error en Role::obtenerTodos - " . $e->getMessage());
            return [];
        }
    }

    /**
     * Obtiene un rol por su nombre
     * 
     * @param string $nombreRol Nombre del rol
     * @return array|false Datos del rol o false si no existe
     */
    public static function obtenerPorNombre($nombreRol)
    {
        try {
            $conexion = Database::getInstance()->getConexion();
            
            $stmt = $conexion->prepare("
                SELECT id_rol, nombre_rol, descripcion
                FROM roles
                WHERE nombre_rol = ?
            ");
            
            $stmt->execute([$nombreRol]);
            return $stmt->fetch();
            
        } catch (PDOException $e) {
            error_log("Error en Role::obtenerPorNombre - " . $e->getMessage());
            return false;
        }
    }

    /**
     * Obtiene los roles de un usuario
     * 
     * @param int $idUsuario ID del usuario
     * @return array Lista de roles del usuario
     */
    public static function obtenerRolesDeUsuario($idUsuario)
    {
        try {
            $conexion = Database::getInstance()->getConexion();
            
            $stmt = $conexion->prepare("
                SELECT r.id_rol, r.nombre_rol, r.descripcion
                FROM roles r
                INNER JOIN usuario_roles ur ON r.id_rol = ur.id_rol
                WHERE ur.id_usuario = ?
            ");
            
            $stmt->execute([$idUsuario]);
            return $stmt->fetchAll();
            
        } catch (PDOException $e) {
            error_log("Error en Role::obtenerRolesDeUsuario - " . $e->getMessage());
            return [];
        }
    }

    /**
     * Asigna un rol a un usuario
     * 
     * @param int $idUsuario ID del usuario
     * @param string $nombreRol Nombre del rol
     * @return bool true si se asignó correctamente
     */
    public static function asignarRol($idUsuario, $nombreRol)
    {
        try {
            // Obtener el ID del rol
            $rol = self::obtenerPorNombre($nombreRol);
            if (!$rol) {
                return false;
            }
            
            $conexion = Database::getInstance()->getConexion();
            
            // Verificar si ya tiene el rol
            $stmt = $conexion->prepare("
                SELECT COUNT(*) as tiene
                FROM usuario_roles
                WHERE id_usuario = ? AND id_rol = ?
            ");
            $stmt->execute([$idUsuario, $rol['id_rol']]);
            $result = $stmt->fetch();
            
            if ($result['tiene'] > 0) {
                return true; // Ya tiene el rol
            }
            
            // Asignar el rol
            $stmt = $conexion->prepare("
                INSERT INTO usuario_roles (id_usuario, id_rol)
                VALUES (?, ?)
            ");
            
            return $stmt->execute([$idUsuario, $rol['id_rol']]);
            
        } catch (PDOException $e) {
            error_log("Error en Role::asignarRol - " . $e->getMessage());
            return false;
        }
    }

    /**
     * Quita un rol de un usuario
     * 
     * @param int $idUsuario ID del usuario
     * @param string $nombreRol Nombre del rol
     * @return bool true si se quitó correctamente
     */
    public static function quitarRol($idUsuario, $nombreRol)
    {
        try {
            // Obtener el ID del rol
            $rol = self::obtenerPorNombre($nombreRol);
            if (!$rol) {
                return false;
            }
            
            $conexion = Database::getInstance()->getConexion();
            
            $stmt = $conexion->prepare("
                DELETE FROM usuario_roles
                WHERE id_usuario = ? AND id_rol = ?
            ");
            
            return $stmt->execute([$idUsuario, $rol['id_rol']]);
            
        } catch (PDOException $e) {
            error_log("Error en Role::quitarRol - " . $e->getMessage());
            return false;
        }
    }

    /**
     * Verifica si un usuario tiene un rol específico
     * 
     * @param int $idUsuario ID del usuario
     * @param string $nombreRol Nombre del rol
     * @return bool true si tiene el rol
     */
    public static function tieneRol($idUsuario, $nombreRol)
    {
        try {
            // Obtener el ID del rol
            $rol = self::obtenerPorNombre($nombreRol);
            if (!$rol) {
                return false;
            }
            
            $conexion = Database::getInstance()->getConexion();
            
            $stmt = $conexion->prepare("
                SELECT COUNT(*) as tiene
                FROM usuario_roles
                WHERE id_usuario = ? AND id_rol = ?
            ");
            
            $stmt->execute([$idUsuario, $rol['id_rol']]);
            $result = $stmt->fetch();
            
            return $result['tiene'] > 0;
            
        } catch (PDOException $e) {
            error_log("Error en Role::tieneRol - " . $e->getMessage());
            return false;
        }
    }

    /**
     * Verifica si un usuario es administrador
     * 
     * @param int $idUsuario ID del usuario
     * @return bool true si es admin
     */
    public static function esAdmin($idUsuario)
    {
        return self::tieneRol($idUsuario, self::ROLE_ADMIN);
    }

    /**
     * Verifica si un usuario es moderador
     * 
     * @param int $idUsuario ID del usuario
     * @return bool true si es moderador
     */
    public static function esModerador($idUsuario)
    {
        return self::tieneRol($idUsuario, self::ROLE_MODERATOR);
    }

    /**
     * Crea los roles básicos si no existen
     * Se recomienda ejecutar una vez al inicializar el sistema
     * 
     * @return bool true si se crearon correctamente
     */
    public static function inicializarRoles()
    {
        try {
            $conexion = Database::getInstance()->getConexion();
            
            $roles = [
                [self::ROLE_USER, 'Usuario normal del sistema'],
                [self::ROLE_MODERATOR, 'Moderador con permisos especiales'],
                [self::ROLE_ADMIN, 'Administrador con acceso total']
            ];
            
            foreach ($roles as $rol) {
                // Verificar si ya existe
                $stmt = $conexion->prepare("
                    SELECT COUNT(*) as existe
                    FROM roles
                    WHERE nombre_rol = ?
                ");
                $stmt->execute([$rol[0]]);
                $result = $stmt->fetch();
                
                if ($result['existe'] == 0) {
                    // Crear el rol
                    $stmt = $conexion->prepare("
                        INSERT INTO roles (nombre_rol, descripcion)
                        VALUES (?, ?)
                    ");
                    $stmt->execute($rol);
                }
            }
            
            return true;
            
        } catch (PDOException $e) {
            error_log("Error en Role::inicializarRoles - " . $e->getMessage());
            return false;
        }
    }
}
