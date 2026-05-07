package com.tow.backend.metrics.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GeoIpServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private GeoIpService geoIpService;

    @Test
    void getCountryFromIp_Success() {
        String ip = "8.8.8.8";
        Map<String, Object> mockResponse = Map.of(
                "status", "success",
                "country", "United States"
        );

        when(restTemplate.getForObject(anyString(), eq(Map.class))).thenReturn(mockResponse);

        String result = geoIpService.getCountryFromIp(ip);

        assertEquals("United States", result);
    }

    @Test
    void getCountryFromIp_FailStatus_ReturnsDesconocida() {
        String ip = "127.0.0.1";
        Map<String, Object> mockResponse = Map.of(
                "status", "fail",
                "message", "reserved range"
        );

        when(restTemplate.getForObject(anyString(), eq(Map.class))).thenReturn(mockResponse);

        String result = geoIpService.getCountryFromIp(ip);

        assertEquals("Desconocida", result);
    }

    @Test
    void getCountryFromIp_ApiError_ReturnsDesconocida() {
        String ip = "8.8.8.8";

        when(restTemplate.getForObject(anyString(), eq(Map.class)))
                .thenThrow(new RestClientException("Connection timeout"));

        String result = geoIpService.getCountryFromIp(ip);

        assertEquals("Desconocida", result);
    }

    @Test
    void getCountryFromIp_NullResponse_ReturnsDesconocida() {
        String ip = "8.8.8.8";

        when(restTemplate.getForObject(anyString(), eq(Map.class))).thenReturn(null);

        String result = geoIpService.getCountryFromIp(ip);

        assertEquals("Desconocida", result);
    }
}
