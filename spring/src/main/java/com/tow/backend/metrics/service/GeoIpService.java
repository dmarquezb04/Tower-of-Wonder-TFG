package com.tow.backend.metrics.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * Servicio encargado de la geolocalización de direcciones IP.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GeoIpService {

    private final RestTemplate restTemplate;

    /**
     * Obtiene el país asociado a una IP.
     * 
     * [Justificación @Cacheable]:
     * Las IPs no cambian de país frecuentemente. Cachear el resultado evita
     * saturar la API externa (ip-api.com) y acelera el proceso.
     * 
     * @param ip dirección IP a consultar
     * @return nombre del país o "Desconocida" si hay error
     */
    @Cacheable(value = "geoIpCache", key = "#ip")
    @SuppressWarnings("unchecked")
    public String getCountryFromIp(String ip) {
        log.debug("Consultando geolocalización externa para IP: {}", ip);
        try {
            String apiUrl = "http://ip-api.com/json/" + ip + "?fields=status,country";
            Map<String, Object> response = restTemplate.getForObject(apiUrl, Map.class);
            
            if (response != null && "success".equals(response.get("status"))) {
                return (String) response.get("country");
            }
        } catch (Exception e) {
            log.warn("Error en geolocalización para IP {}: {}", ip, e.getMessage());
        }
        return "Desconocida";
    }
}
