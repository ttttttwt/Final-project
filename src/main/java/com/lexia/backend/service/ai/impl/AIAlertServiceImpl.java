package com.lexia.backend.service.ai.impl;

import com.lexia.backend.entity.AIAlert;
import com.lexia.backend.repository.AIAlertRepository;
import com.lexia.backend.service.ai.AIAlertService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AIAlertServiceImpl implements AIAlertService {

    private final AIAlertRepository alertRepository;

    @Override
    public List<AIAlert> getUnreadAlerts() {
        return alertRepository.findByIsReadFalseOrderByCreatedAtDesc();
    }

    @Override
    @Transactional
    public void markAsRead(Long alertId) {
        AIAlert alert = alertRepository.findById(alertId)
                .orElseThrow(() -> new RuntimeException("Alert not found: " + alertId));
        alert.setIsRead(true);
        alertRepository.save(alert);
    }

    @Override
    @Transactional
    public void createAlert(String type, String message, String severity) {
        AIAlert alert = AIAlert.builder()
                .alertType(type)
                .message(message)
                .severity(severity)
                .isRead(false)
                .build();
        alertRepository.save(alert);
    }
}
