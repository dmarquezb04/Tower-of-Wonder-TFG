import { useEffect } from 'react';
import { useLocation } from 'react-router-dom';
import { trackVisit } from '../api/metricsApi';

/**
 * Hook que registra automáticamente las visitas al cambiar de ruta.
 * Excluye las rutas de administración.
 */
export const useTrackVisit = () => {
    const location = useLocation();

    useEffect(() => {
        const path = location.pathname;

        // No rastreamos si estamos en el panel de administración
        if (path.startsWith('/admin')) {
            return;
        }

        // Registramos la visita
        trackVisit(path);
        
    }, [location.pathname]); // Se dispara cada vez que cambia el pathname
};
