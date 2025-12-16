package com.lexia.backend.service.ai;

import com.lexia.backend.entity.AIAlert;
import java.util.List;

public interface AIAlertService {
    List<AIAlert> getUnreadAlerts();
    void markAsRead(Long alertId);
    void createAlert(String type, String message, String severity);
}
