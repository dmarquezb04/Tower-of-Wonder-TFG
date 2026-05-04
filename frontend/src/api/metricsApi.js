import axios from 'axios';

const API_URL = '/api/metrics';

/**
 * Registra una visita en el backend.
 * @param {string} url - La ruta actual (pathname)
 */
export const trackVisit = async (url) => {
    try {
        await axios.post(`${API_URL}/track`, { url });
    } catch (error) {
        // Fallo silencioso para no molestar al usuario si falla el tracking
        console.error('Error tracking visit:', error);
    }
};

/**
 * Obtiene las estadísticas de métricas (solo Admin).
 * @param {string} token - Token JWT del admin
 */
export const getAdminMetricsStats = async (token) => {
    const response = await axios.get(`${API_URL}/admin/stats`, {
        headers: { Authorization: `Bearer ${token}` }
    });
    return response.data;
};
