package com.tow.backend.user.service;

import com.tow.backend.exception.BadRequestException;
import com.tow.backend.exception.ConflictException;
import com.tow.backend.exception.NotFoundException;
import com.tow.backend.user.dto.TwoFactorSetupDTO;
import com.tow.backend.user.dto.UpdateProfileRequest;
import com.tow.backend.user.dto.UserProfileDTO;

/**
 * Contrato del servicio de gestiÃ³n del perfil y cuenta del usuario.
 *
 * <p>Cubre operaciones de consulta de perfil, configuraciÃ³n de 2FA,
 * actualizaciÃ³n de datos y gestiÃ³n del ciclo de vida de la cuenta.
 *
 * @author DarÃ­o MÃ¡rquez Bautista
 */
public interface UserService {

    /**
     * Devuelve el perfil pÃºblico del usuario autenticado.
     *
     * @param email email del usuario autenticado (obtenido del token JWT)
     * @return DTO con los datos del perfil
     * @throws NotFoundException si no existe ningÃºn usuario con ese email
     */
    UserProfileDTO getUserProfile(String email);

    /**
     * Genera un nuevo secret TOTP y el URI para el cÃ³digo QR de Google Authenticator.
     *
     * <p>El secret generado es provisional hasta que el usuario lo confirme mediante
     * {@link #enableTwoFactor}. No se persiste en este paso.
     *
     * @param email email del usuario autenticado
     * @return DTO con el secret Base32 y el URI del cÃ³digo QR
     * @throws NotFoundException   si el usuario no existe
     * @throws ConflictException   si el usuario ya tiene 2FA activo
     */
    TwoFactorSetupDTO generateTwoFactorSetup(String email);

    /**
     * Activa el 2FA para el usuario tras verificar que el cÃ³digo TOTP es correcto.
     *
     * @param email  email del usuario autenticado
     * @param secret secret Base32 generado en {@link #generateTwoFactorSetup}
     * @param code   cÃ³digo TOTP de 6 dÃ­gitos generado por la app del usuario
     * @throws NotFoundException   si el usuario no existe
     * @throws ConflictException   si el 2FA ya estÃ¡ activo
     * @throws BadRequestException si el cÃ³digo no es numÃ©rico o es incorrecto
     */
    void enableTwoFactor(String email, String secret, String code);

    /**
     * Desactiva el 2FA para el usuario tras verificar el cÃ³digo TOTP actual.
     *
     * @param email email del usuario autenticado
     * @param code  cÃ³digo TOTP actual de 6 dÃ­gitos para confirmar la operaciÃ³n
     * @throws NotFoundException   si el usuario no existe
     * @throws BadRequestException si el 2FA no estÃ¡ activo, el cÃ³digo no es numÃ©rico o es incorrecto
     */
    void disableTwoFactor(String email, String code);

    /**
     * Realiza un borrado lÃ³gico de la cuenta (soft delete).
     *
     * <p>La cuenta queda marcada como inactiva y se genera un token de recuperaciÃ³n
     * que se envÃ­a al email del usuario. El token expira a los 7 dÃ­as.
     *
     * @param email email del usuario a desactivar
     * @throws NotFoundException si el usuario no existe
     */
    void deleteAccount(String email);

    /**
     * Reactiva una cuenta previamente desactivada mediante el token de recuperaciÃ³n.
     *
     * @param token token de recuperaciÃ³n enviado por email
     * @throws NotFoundException   si el token no corresponde a ninguna cuenta
     * @throws BadRequestException si el token ha caducado
     */
    void reactivateAccount(String token);

    /**
     * Actualiza el nombre de usuario y/o contraseÃ±a del usuario autenticado.
     *
     * <p>Para cambiar la contraseÃ±a es obligatorio proporcionar la contraseÃ±a actual.
     * Si no hay cambios, no realiza ninguna operaciÃ³n.
     *
     * @param currentEmail email actual del usuario (del token JWT)
     * @param request      DTO con los nuevos datos (username, currentPassword, newPassword)
     * @throws NotFoundException   si el usuario no existe
     * @throws BadRequestException si se intenta cambiar la contraseÃ±a sin proporcionar la actual
     *                             o si la contraseÃ±a actual es incorrecta
     */
    void updateProfile(String currentEmail, UpdateProfileRequest request);
}


