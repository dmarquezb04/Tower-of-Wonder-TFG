package com.tow.backend.metrics.service;

import com.tow.backend.metrics.entity.PageView;
import com.tow.backend.metrics.repository.PageViewRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class MetricsService {

    private final PageViewRepository pageViewRepository;

    @Transactional
    public void trackVisit(String url, HttpServletRequest request) {
        String ip = getClientIp(request);
        String userAgent = request.getHeader("User-Agent");
        
        // Detección básica de zona local
        String zona = "Desconocida";
        if (ip.equals("127.0.0.1") || ip.equals("0:0:0:0:0:0:0:1")) {
            zona = "Localhost";
        }

        PageView view = PageView.builder()
                .url(url)
                .fecha(LocalDateTime.now())
                .ip(ip)
                .navegador(userAgent)
                .zona(zona)
                .build();

        pageViewRepository.save(view);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("porUrl", pageViewRepository.countVisitsByUrl());
        stats.put("porZona", pageViewRepository.countVisitsByZona());
        stats.put("total", pageViewRepository.count());
        return stats;
    }

    private String getClientIp(HttpServletRequest request) {
        String remoteAddr = request.getHeader("X-Forwarded-For");
        if (remoteAddr == null || remoteAddr.isEmpty()) {
            remoteAddr = request.getRemoteAddr();
        } else {
            // X-Forwarded-For puede contener una lista de IPs, cogemos la primera
            remoteAddr = remoteAddr.split(",")[0].trim();
        }
        return remoteAddr;
    }
}
