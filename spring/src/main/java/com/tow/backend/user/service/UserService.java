package com.tow.backend.user.service;

import com.tow.backend.exception.BadRequestException;
import com.tow.backend.exception.ConflictException;
import com.tow.backend.exception.NotFoundException;
import com.tow.backend.user.dto.TwoFactorSetupDTO;
import com.tow.backend.user.dto.UpdateProfileRequest;
import com.tow.backend.user.dto.UserProfileDTO;

/**
 * Contrato del servicio de gestión del perfil y cuenta del usuario.
 *
 * <p>Cubre operaciones de consulta de perfil, configuración de 2FA,
 * actualización de datos y gestión del ciclo de vida de la cuenta.
 *
 * @author Darío Márquez Bautista
 */
public interface UserService {

    /**
     * Devuelve el perfil público del usuario autenticado.
     *
     * @param email email del usuario autenticado (obtenido del token JWT)
     * @return DTO con los datos del perfil
     * @throws NotFoundException si no existe ningún usuario con ese email
     */
    UserProfileDTO getUserProfile(String email);

    /**
     * Genera un nuevo secret TOTP y el URI para el código QR de Google Authenticator.
     *
     * <p>El secret generado es provisional hasta que el usuario lo confirme mediante
     * {@link #enableTwoFactor}. No se persiste en este paso.
     *
     * @param email email del usuario autenticado
     * @return DTO con el secret Base32 y el URI del código QR
     * @throws NotFoundException   si el usuario no existe
     * @throws ConflictException   si el usuario ya tiene 2FA activo
     */
    TwoFactorSetupDTO generateTwoFactorSetup(String email);

    /**
     * Activa el 2FA para el usuario tras verificar que el código TOTP es correcto.
     *
     * @param email  email del usuario autenticado
     * @param secret secret Base32 generado en {@link #generateTwoFactorSetup}
     * @param code   código TOTP de 6 dígitos generado por la app del usuario
     * @throws NotFoundException   si el usuario no existe
     * @throws ConflictException   si el 2FA ya está activo
     * @throws BadRequestException si el código no es numérico o es incorrecto
     */
    void enableTwoFactor(String email, String secret, String code);

    /**
     * Desactiva el 2FA para el usuario tras verificar el código TOTP actual.
     *
     * @param email email del usuario autenticado
     * @param code  código TOTP actual de 6 dígitos para confirmar la operación
     * @throws NotFoundException   si el usuario no existe
     * @throws BadRequestException si el 2FA no está activo, el código no es numérico o es incorrecto
     */
    void disableTwoFactor(String email, String code);

    /**
     * Realiza un borrado lógico de la cuenta (soft delete).
     *
     * <p>La cuenta queda marcada como inactiva y se genera un token de recuperación
     * que se envía al email del usuario. El token expira a los 7 días.
     *
     * @param email email del usuario a desactivar
     * @throws NotFoundException si el usuario no existe
     */
    void deleteAccount(String email);

    /**
     * Reactiva una cuenta previamente desactivada mediante el token de recuperación.
     *
     * @param token token de recuperación enviado por email
     * @throws NotFoundException   si el token no corresponde a ninguna cuenta
     * @throws BadRequestException si el token ha caducado
     */
    void reactivateAccount(String token);

    /**
     * Actualiza el nombre de usuario y/o contraseña del usuario autenticado.
     *
     * <p>Para cambiar la contraseña es obligatorio proporcionar la contraseña actual.
     * Si no hay cambios, no realiza ninguna operación.
     *
     * @param currentEmail email actual del usuario (del token JWT)
     * @param request      DTO con los nuevos datos (username, currentPassword, newPassword)
     * @throws NotFoundException   si el usuario no existe
     * @throws BadRequestException si se intenta cambiar la contraseña sin proporcionar la actual
     *                             o si la contraseña actual es incorrecta
     */
    void updateProfile(String currentEmail, UpdateProfileRequest request);
}


