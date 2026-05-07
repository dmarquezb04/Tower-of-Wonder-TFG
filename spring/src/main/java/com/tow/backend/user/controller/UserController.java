package com.tow.backend.user.controller;

import com.tow.backend.common.dto.ApiResponse;
import com.tow.backend.user.dto.TwoFactorCodeDTO;
import com.tow.backend.user.dto.TwoFactorSetupDTO;
import com.tow.backend.user.dto.UpdateProfileRequest;
import com.tow.backend.user.dto.UserProfileDTO;
import com.tow.backend.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST para la gestiÃ³n del perfil y cuenta del usuario autenticado.
 *
 * <p>Todos los endpoints requieren autenticaciÃ³n JWT.
 * Los errores de negocio son gestionados por {@link com.tow.backend.exception.GlobalExceptionHandler}.
 *
 * @author DarÃ­o MÃ¡rquez Bautista
 */
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
@Tag(name = "Usuario", description = "Perfil, 2FA y gestiÃ³n de cuenta del usuario autenticado")
public class UserController {

    private final UserService userService;

    /**
     * Devuelve el perfil del usuario autenticado.
     *
     * @param userDetails usuario autenticado (inyectado por Spring Security)
     * @return 200 OK con los datos del perfil
     */
    @GetMapping("/profile")
    @Operation(summary = "Obtener perfil del usuario autenticado")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Perfil devuelto correctamente"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "No autenticado"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    })
    public ResponseEntity<UserProfileDTO> getProfile(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(userService.getUserProfile(userDetails.getUsername()));
    }

    /**
     * Genera un nuevo secret TOTP y el URI del cÃ³digo QR para configurar Google Authenticator.
     *
     * @param userDetails usuario autenticado
     * @return 200 OK con el secret y el URI del QR
     */
    @GetMapping("/2fa/setup")
    @Operation(summary = "Iniciar configuraciÃ³n de 2FA (genera secret y QR)")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Setup de 2FA generado"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "El 2FA ya estÃ¡ activo")
    })
    public ResponseEntity<TwoFactorSetupDTO> setupTwoFactor(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(userService.generateTwoFactorSetup(userDetails.getUsername()));
    }

    /**
     * Activa el 2FA tras verificar que el cÃ³digo TOTP introducido es correcto.
     *
     * @param userDetails usuario autenticado
     * @param request     body con el secret y el cÃ³digo TOTP de confirmaciÃ³n
     * @return 200 OK con mensaje de confirmaciÃ³n
     */
    @PostMapping("/2fa/enable")
    @Operation(summary = "Activar 2FA con el cÃ³digo de confirmaciÃ³n")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "2FA activado correctamente"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "CÃ³digo invÃ¡lido o incorrecto"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "El 2FA ya estÃ¡ activo")
    })
    public ResponseEntity<ApiResponse> enableTwoFactor(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody TwoFactorCodeDTO request) {
        userService.enableTwoFactor(userDetails.getUsername(), request.getSecret(), request.getCode());
        return ResponseEntity.ok(new ApiResponse("2FA activado correctamente"));
    }

    /**
     * Desactiva el 2FA verificando el cÃ³digo TOTP actual del usuario.
     *
     * @param userDetails usuario autenticado
     * @param request     body con el cÃ³digo TOTP actual
     * @return 200 OK con mensaje de confirmaciÃ³n
     */
    @PostMapping("/2fa/disable")
    @Operation(summary = "Desactivar 2FA con el cÃ³digo actual")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "2FA desactivado correctamente"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "CÃ³digo invÃ¡lido, incorrecto o 2FA no activo")
    })
    public ResponseEntity<ApiResponse> disableTwoFactor(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody TwoFactorCodeDTO request) {
        userService.disableTwoFactor(userDetails.getUsername(), request.getCode());
        return ResponseEntity.ok(new ApiResponse("2FA desactivado correctamente"));
    }

    /**
     * Realiza el borrado lÃ³gico de la cuenta del usuario autenticado.
     *
     * <p>La cuenta se desactiva y se envÃ­a un email con un enlace de recuperaciÃ³n
     * vÃ¡lido durante 7 dÃ­as.
     *
     * @param userDetails usuario autenticado
     * @return 200 OK con mensaje de confirmaciÃ³n
     */
    @DeleteMapping("/me")
    @Operation(summary = "Desactivar cuenta (borrado lÃ³gico con opciÃ³n de recuperaciÃ³n)")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Cuenta desactivada correctamente"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    })
    public ResponseEntity<ApiResponse> deleteMe(@AuthenticationPrincipal UserDetails userDetails) {
        userService.deleteAccount(userDetails.getUsername());
        return ResponseEntity.ok(new ApiResponse("Cuenta desactivada correctamente"));
    }

    /**
     * Actualiza el nombre de usuario y/o contraseÃ±a del usuario autenticado.
     *
     * @param userDetails usuario autenticado
     * @param request     body con los campos a actualizar
     * @return 200 OK con mensaje de confirmaciÃ³n
     */
    @PutMapping("/profile")
    @Operation(summary = "Actualizar nombre de usuario y/o contraseÃ±a")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Perfil actualizado correctamente"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "ContraseÃ±a actual incorrecta o campos invÃ¡lidos"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    })
    public ResponseEntity<ApiResponse> updateProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody UpdateProfileRequest request) {
        userService.updateProfile(userDetails.getUsername(), request);
        return ResponseEntity.ok(new ApiResponse("Perfil actualizado correctamente"));
    }
}


