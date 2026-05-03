package com.tow.backend.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminMetricsDTO {
    private long totalUsers;
    private long activeUsers;
    private long usersWith2FA;
}
