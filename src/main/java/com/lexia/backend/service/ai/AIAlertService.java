package com.lexia.backend.service.ai;

import com.lexia.backend.entity.AIAlert;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.Duration;
import java.util.List;

public interface AIAlertService {
    List<AIAlert> getUnreadAlerts();
    Page<AIAlert> getAlerts(Boolean isRead, String severity, String type, Pageable pageable);
    void markAsRead(Long alertId);
    void markAllAsRead();
    void createAlert(String type, String message, String severity);
    boolean hasRecentAlert(String type, String severity, Duration duration);
}
