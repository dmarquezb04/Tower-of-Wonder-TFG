import apiClient, { getAuthHeader } from './apiClient';

/**
 * API para el formulario de contacto público.
 */

/**
 * Envía un mensaje de contacto al servidor.
 * 
 * @param {object} contactData {nombre, email, asunto, mensaje}
 * @param {string} token (opcional) para identificar al usuario autenticado
 */
export async function sendContactMessage(contactData, token = null) {
  const config = token ? getAuthHeader(token) : {};
  const response = await apiClient.post('/contacto', contactData, config);
  return response.data;
}
