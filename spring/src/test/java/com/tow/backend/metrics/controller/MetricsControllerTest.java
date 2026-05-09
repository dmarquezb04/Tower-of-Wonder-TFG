package com.tow.backend.metrics.controller;

import com.tow.backend.metrics.dto.TrackVisitRequest;
import com.tow.backend.metrics.service.MetricsService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;
import java.util.Objects;

@ExtendWith(MockitoExtension.class)
class MetricsControllerTest {

    @Mock
    private MetricsService metricsService;

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private MetricsController metricsController;

    @BeforeEach
    void setUp() {
    }

    @Test
    void track_PublicUrl_CallsService() {
        TrackVisitRequest visitRequest = new TrackVisitRequest();
        visitRequest.setUrl("/home");

        when(request.getHeader("X-Forwarded-For")).thenReturn("1.2.3.4");
        when(request.getHeader("User-Agent")).thenReturn("Mozilla/5.0");
        
        doNothing().when(metricsService).trackVisit(eq("/home"), eq("1.2.3.4"), eq("Mozilla/5.0"));

        ResponseEntity<Void> response = metricsController.track(visitRequest, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(metricsService).trackVisit(eq("/home"), eq("1.2.3.4"), eq("Mozilla/5.0"));
    }

    @Test
    void track_AdminUrl_DoesNotCallService() {
        TrackVisitRequest visitRequest = new TrackVisitRequest();
        visitRequest.setUrl("/admin/dashboard");

        ResponseEntity<Void> response = metricsController.track(visitRequest, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(metricsService, never()).trackVisit(anyString(), anyString(), anyString());
    }

    @Test
    void getAdminStats_ReturnsStats() {
        Map<String, Object> mockStats = Map.of("total", 100L);
        when(metricsService.getStats()).thenReturn(mockStats);

        ResponseEntity<Map<String, Object>> response = metricsController.getAdminStats();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(100L, Objects.requireNonNull(response.getBody()).get("total"));
    }

    @Test
    void exportMetrics_ReturnsExcelFile() {
        byte[] mockExcelData = new byte[]{1, 2, 3};
        when(metricsService.exportToExcel()).thenReturn(mockExcelData);

        ResponseEntity<Resource> response = metricsController.exportMetrics();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", 
                     response.getHeaders().getContentType().toString());
        assertTrue(response.getHeaders().getContentDisposition().toString().contains("metricas_visitas.xlsx"));
    }
}
