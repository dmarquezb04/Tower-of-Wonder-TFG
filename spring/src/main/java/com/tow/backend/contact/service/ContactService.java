package com.tow.backend.contact.service;

import com.tow.backend.contact.dto.ContactRequest;

/**
 * Contrato del servicio de gestión de contacto.
 *
 * <p>Se encarga de procesar los mensajes enviados a través del formulario
 * de contacto, guardándolos en el sistema y notificando a los administradores.
 *
 * @author Darío Márquez Bautista
 */
public interface ContactService {

    /**
     * Procesa una solicitud de contacto.
     *
     * <p>Guarda el mensaje en la base de datos y envía una notificación
     * por email al administrador del sistema.
     *
     * @param request DTO con los datos del remitente y el mensaje
     */
    void processContactMessage(ContactRequest request);
}


