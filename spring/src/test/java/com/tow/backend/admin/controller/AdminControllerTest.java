package com.tow.backend.admin.controller;

import com.tow.backend.admin.dto.AdminMetricsDTO;
import com.tow.backend.admin.dto.UserAdminDTO;
import com.tow.backend.admin.service.AdminService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

class AdminControllerTest {

    @Mock
    private AdminService adminService;

    @InjectMocks
    private AdminController adminController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getAllUsers_ReturnsList() {
        List<UserAdminDTO> expectedList = Collections.singletonList(new UserAdminDTO());
        when(adminService.getAllUsers()).thenReturn(expectedList);

        ResponseEntity<List<UserAdminDTO>> response = adminController.getAllUsers();

        assertEquals(200, response.getStatusCode().value());
        assertEquals(expectedList, response.getBody());
    }

    @Test
    void getMetrics_ReturnsMetrics() {
        AdminMetricsDTO expectedMetrics = new AdminMetricsDTO();
        when(adminService.getMetrics()).thenReturn(expectedMetrics);

        ResponseEntity<AdminMetricsDTO> response = adminController.getMetrics();

        assertEquals(200, response.getStatusCode().value());
        assertEquals(expectedMetrics, response.getBody());
    }
}


