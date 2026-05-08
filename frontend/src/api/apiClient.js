import axios from 'axios';

const API_BASE = import.meta.env.VITE_API_URL || "/api";

/**
 * Instancia central de Axios para toda la aplicación.
 * Configurada con un interceptor que extrae automáticamente los mensajes
 * de error personalizados del backend (ErrorResponse.java).
 */
const apiClient = axios.create({
  baseURL: API_BASE,
  headers: {
    'Content-Type': 'application/json'
  }
});

// Interceptor de respuesta para estandarizar el manejo de errores
apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    // 1. El servidor respondió con un error (4xx, 5xx)
    if (error.response && error.response.data) {
      const data = error.response.data;

      // Prioridad 1: Campo 'error' de nuestro ErrorResponse.java
      // Prioridad 2: Campo 'message' estándar de Spring
      const serverMessage = data.error || data.message;

      // Si hay errores de validación (Bean Validation), los concatenamos
      if (data.validationErrors) {
        const fieldErrors = Object.entries(data.validationErrors)
          .map(([field, msg]) => `${field}: ${msg}`)
          .join(' | ');
        return Promise.reject(new Error(`${serverMessage} (${fieldErrors})`));
      }

      return Promise.reject(new Error(serverMessage || 'Error desconocido en el servidor'));
    }

    // 2. Error de conexión (el servidor no respondió)
    if (error.request) {
      return Promise.reject(new Error('No se pudo conectar con el servidor. Revisa tu conexión.'));
    }

    // 3. Otros errores (configuración, etc.)
    return Promise.reject(error);
  }
);

/**
 * Helper para obtener las cabeceras de autenticación JWT.
 */
export const getAuthHeader = (token) => ({
  headers: {
    'Authorization': `Bearer ${token}`
  }
});

export default apiClient;
