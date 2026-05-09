package com.tow.backend.metrics.service;

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
     * @param url       ruta de la página visitada
     * @param ip        dirección IP del cliente
     * @param userAgent cadena del navegador del cliente
     */
    void trackVisit(String url, String ip, String userAgent);

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
