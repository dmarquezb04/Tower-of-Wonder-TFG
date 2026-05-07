package com.tow.backend.admin.controller;

import com.tow.backend.admin.dto.AdminMetricsDTO;
import com.tow.backend.admin.dto.UserAdminDTO;
import com.tow.backend.admin.service.AdminService;
import com.tow.backend.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para las operaciones de administración de usuarios.
 *
 * <p>Todos los endpoints requieren el rol ADMIN.
 * Los errores de negocio son gestionados por {@link com.tow.backend.exception.GlobalExceptionHandler}.
 *
 * @author Darío Márquez Bautista
 */
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@Tag(name = "Administración", description = "Gestión de usuarios y métricas del sistema (solo ADMIN)")
public class AdminController {

    private final AdminService adminService;

    /**
     * Devuelve la lista completa de usuarios del sistema.
     *
     * @return 200 OK con lista de DTOs de usuario
     */
    @GetMapping("/users")
    @Operation(summary = "Listar todos los usuarios")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Lista devuelta correctamente"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Acceso denegado — se requiere rol ADMIN")
    })
    public ResponseEntity<List<UserAdminDTO>> getAllUsers() {
        return ResponseEntity.ok(adminService.getAllUsers());
    }

    /**
     * Actualiza los datos de un usuario (username, rol y estado).
     *
     * @param id       ID del usuario a actualizar
     * @param username nuevo nombre de usuario
     * @param role     nombre del nuevo rol
     * @param activo   nuevo estado activo/inactivo
     * @return 200 OK con mensaje de confirmación
     */
    @PutMapping("/users/{id}")
    @Operation(summary = "Actualizar datos de un usuario")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Usuario actualizado correctamente"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Acceso denegado — se requiere rol ADMIN"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Usuario o rol no encontrado")
    })
    public ResponseEntity<ApiResponse> updateUser(
            @PathVariable Long id,
            @RequestParam String username,
            @RequestParam String role,
            @RequestParam boolean activo) {
        adminService.updateUser(id, username, role, activo);
        return ResponseEntity.ok(new ApiResponse("Usuario actualizado con éxito"));
    }

    /**
     * Elimina permanentemente un usuario del sistema.
     *
     * @param id ID del usuario a eliminar
     * @return 200 OK con mensaje de confirmación
     */
    @DeleteMapping("/users/{id}")
    @Operation(summary = "Eliminar un usuario (hard delete)")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Usuario eliminado correctamente"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Acceso denegado — se requiere rol ADMIN"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    })
    public ResponseEntity<ApiResponse> deleteUser(@PathVariable Long id) {
        adminService.deleteUser(id);
        return ResponseEntity.ok(new ApiResponse("Usuario eliminado con éxito"));
    }

    /**
     * Devuelve métricas generales del sistema.
     *
     * @return 200 OK con las métricas
     */
    @GetMapping("/metrics")
    @Operation(summary = "Obtener métricas del sistema")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Métricas devueltas correctamente"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Acceso denegado — se requiere rol ADMIN")
    })
    public ResponseEntity<AdminMetricsDTO> getMetrics() {
        return ResponseEntity.ok(adminService.getMetrics());
    }
}


