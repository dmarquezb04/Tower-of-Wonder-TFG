import apiClient, { getAuthHeader } from './apiClient';

/**
 * metricsApi.js — Registro de visitas y estadísticas.
 */

const API_URL = '/metrics';

/**
 * Registra una visita en el backend.
 * @param {string} url - La ruta actual (pathname)
 */
export const trackVisit = async (url) => {
    try {
        await apiClient.post(`${API_URL}/track`, { url });
    } catch (error) {
        // Fallo silencioso para no molestar al usuario si falla el tracking
        console.warn('Silent tracking failure:', error.message);
    }
};

/**
 * Obtiene las estadísticas de métricas (solo Admin).
 * @param {string} token - Token JWT del admin
 */
export const getAdminMetricsStats = async (token) => {
    const response = await apiClient.get(`${API_URL}/admin/stats`, getAuthHeader(token));
    return response.data;
};
