package com.lexia.backend.dto.ai;

import com.lexia.backend.entity.AIAlert;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlertListResponse {
    private List<AIAlert> content;
    private long totalElements;
    private long unreadCount;
}
