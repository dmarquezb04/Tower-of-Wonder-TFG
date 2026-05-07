package com.tow.backend.metrics.service;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * Contrato del servicio de métricas y analítica.
 *
 * <p>Permite registrar visitas a las distintas URLs de la aplicación
 * y obtener estadísticas agregadas para el panel de administración.
 *
 * @author Darío Márquez Bautista
 */
public interface MetricsService {

    /**
     * Registra una visita a una URL específica.
     *
     * <p>Extrae la IP y el User-Agent de la petición para enriquecer
     * los datos de la métrica.
     *
     * @param url     ruta de la página visitada
     * @param request petición HTTP actual
     */
    void trackVisit(String url, HttpServletRequest request);

    /**
     * Obtiene estadísticas generales del sistema.
     *
     * @return mapa con contadores por URL, por zona geográfica y total
     */
    Map<String, Object> getStats();

    /**
     * Genera un archivo Excel (.xlsx) con el listado completo de visitas.
     *
     * @return array de bytes con el contenido del archivo Excel
     */
    byte[] exportToExcel();
}
