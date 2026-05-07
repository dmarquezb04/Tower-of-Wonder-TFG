package com.tow.backend.metrics.service;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * Contrato del servicio de mÃ©tricas y analÃ­tica.
 *
 * <p>Permite registrar visitas a las distintas URLs de la aplicaciÃ³n
 * y obtener estadÃ­sticas agregadas para el panel de administraciÃ³n.
 *
 * @author DarÃ­o MÃ¡rquez Bautista
 */
public interface MetricsService {

    /**
     * Registra una visita a una URL especÃ­fica.
     *
     * <p>Extrae la IP y el User-Agent de la peticiÃ³n para enriquecer
     * los datos de la mÃ©trica.
     *
     * @param url     ruta de la pÃ¡gina visitada
     * @param request peticiÃ³n HTTP actual
     */
    void trackVisit(String url, HttpServletRequest request);

    /**
     * Obtiene estadÃ­sticas generales del sistema.
     *
     * @return mapa con contadores por URL, por zona geogrÃ¡fica y total
     */
    Map<String, Object> getStats();
}


