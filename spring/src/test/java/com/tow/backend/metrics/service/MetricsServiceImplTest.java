package com.tow.backend.metrics.service;

import com.tow.backend.metrics.entity.PageView;
import com.tow.backend.metrics.repository.PageViewRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MetricsServiceImplTest {

    @Mock
    private PageViewRepository pageViewRepository;

    @Mock
    private GeoIpService geoIpService;

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private MetricsServiceImpl metricsService;

    @BeforeEach
    void setUp() {
    }

    @Test
    void trackVisit_Localhost_RecordsLocalhostZone() {
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        when(request.getHeader("User-Agent")).thenReturn("Mozilla/5.0");

        metricsService.trackVisit("/home", request);

        ArgumentCaptor<PageView> captor = ArgumentCaptor.forClass(PageView.class);
        verify(pageViewRepository).save(captor.capture());
        PageView savedView = captor.getValue();

        assertEquals("/home", savedView.getUrl());
        assertEquals("127.0.0.1", savedView.getIp());
        assertEquals("Mozilla/5.0", savedView.getNavegador());
        assertEquals("Localhost", savedView.getZona());
        assertNotNull(savedView.getFecha());
    }

    @Test
    void trackVisit_ExternalIp_CallsGeoIpService() {
        when(request.getHeader("X-Forwarded-For")).thenReturn("8.8.8.8");
        when(request.getHeader("User-Agent")).thenReturn("Chrome");
        when(geoIpService.getCountryFromIp("8.8.8.8")).thenReturn("United States");

        metricsService.trackVisit("/shop", request);

        ArgumentCaptor<PageView> captor = ArgumentCaptor.forClass(PageView.class);
        verify(pageViewRepository).save(captor.capture());
        PageView savedView = captor.getValue();

        assertEquals("/shop", savedView.getUrl());
        assertEquals("8.8.8.8", savedView.getIp());
        assertEquals("Chrome", savedView.getNavegador());
        assertEquals("United States", savedView.getZona());
    }

    @Test
    void getStats_ReturnsAggregatedData() {
        when(pageViewRepository.countVisitsByUrl()).thenReturn(Collections.emptyList());
        when(pageViewRepository.countVisitsByZona()).thenReturn(Collections.emptyList());
        when(pageViewRepository.count()).thenReturn(10L);

        Map<String, Object> stats = metricsService.getStats();

        assertEquals(10L, stats.get("total"));
        assertNotNull(stats.get("porUrl"));
        assertNotNull(stats.get("porZona"));
    }

    @Test
    void exportToExcel_GeneratesByteArray() {
        PageView view1 = PageView.builder().id(1L).url("/home").ip("8.8.8.8").fecha(LocalDateTime.now()).build();
        when(pageViewRepository.findAllByOrderByFechaDesc()).thenReturn(Arrays.asList(view1));
        when(pageViewRepository.countVisitsByUrl()).thenReturn(Collections.emptyList());
        when(pageViewRepository.countVisitsByZona()).thenReturn(Collections.emptyList());

        byte[] excelData = metricsService.exportToExcel();

        assertNotNull(excelData);
        assertTrue(excelData.length > 0);
    }
}
