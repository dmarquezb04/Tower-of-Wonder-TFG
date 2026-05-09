package com.tow.backend.metrics.service;

import com.tow.backend.metrics.entity.PageView;
import com.tow.backend.metrics.repository.PageViewRepository;
import com.tow.backend.metrics.util.MetricsExcelExporter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementación del servicio de métricas.
 *
 * @see MetricsService
 * @author Darío Márquez Bautista
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MetricsServiceImpl implements MetricsService {

    private final PageViewRepository pageViewRepository;
    private final GeoIpService geoIpService;

    /**
     * Registra una visita de forma asíncrona.
     * 
     * [Justificación @Async]: 
     * El registro de métricas no debe bloquear la respuesta al usuario. 
     * Se ejecuta en un hilo separado.
     */
    @Override
    @Async
    @Transactional
    public void trackVisit(String url, String ip, String userAgent) {
        String zona = "Desconocida";
        if (ip.equals("127.0.0.1") || ip.equals("0:0:0:0:0:0:0:1")) {
            zona = "Localhost";
        } else {
            zona = geoIpService.getCountryFromIp(ip);
        }

        PageView view = PageView.builder()
                .url(url)
                .fecha(LocalDateTime.now())
                .ip(ip)
                .navegador(userAgent)
                .zona(zona)
                .build();

        pageViewRepository.save(view);
        log.debug("Visita registrada asíncronamente: {} desde {}", url, zona);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("porUrl", pageViewRepository.countVisitsByUrl());
        stats.put("porZona", pageViewRepository.countVisitsByZona());
        stats.put("total", pageViewRepository.count());
        return stats;
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] exportToExcel() {
        List<PageView> views = pageViewRepository.findAllByOrderByFechaDesc();
        List<Map<String, Object>> statsByUrl = pageViewRepository.countVisitsByUrl();
        List<Map<String, Object>> statsByZona = pageViewRepository.countVisitsByZona();

        return MetricsExcelExporter.export(views, statsByUrl, statsByZona);
    }
}
