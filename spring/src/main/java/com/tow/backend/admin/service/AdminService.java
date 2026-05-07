package com.tow.backend.admin.service;

import com.tow.backend.admin.dto.AdminMetricsDTO;
import com.tow.backend.admin.dto.UserAdminDTO;
import com.tow.backend.exception.NotFoundException;

import java.util.List;

/**
 * Contrato del servicio de administración del panel de control.
 *
 * <p>Proporciona operaciones de gestión de usuarios y métricas del sistema,
 * accesibles únicamente por usuarios con rol ADMIN.
 *
 * @author Darío Márquez Bautista
 */
public interface AdminService {

    /**
     * Devuelve métricas generales del sistema: total de usuarios, activos y con 2FA.
     *
     * @return DTO con las métricas del sistema
     */
    AdminMetricsDTO getMetrics();

    /**
     * Devuelve la lista completa de usuarios registrados en el sistema.
     *
     * @return lista de DTOs de usuarios con sus datos administrativos
     */
    List<UserAdminDTO> getAllUsers();

    /**
     * Actualiza los datos de un usuario (username, rol y estado activo).
     *
     * @param id       ID del usuario a actualizar
     * @param username nuevo nombre de usuario
     * @param roleName nombre del nuevo rol (ej. "user", "admin")
     * @param activo   nuevo estado de la cuenta
     * @throws NotFoundException si no existe ningún usuario con ese ID
     * @throws NotFoundException si el rol especificado no existe
     */
    void updateUser(Long id, String username, String roleName, boolean activo);

    /**
     * Elimina permanentemente un usuario del sistema (hard delete).
     *
     * @param id ID del usuario a eliminar
     * @throws NotFoundException si no existe ningún usuario con ese ID
     */
    void deleteUser(Long id);
}


