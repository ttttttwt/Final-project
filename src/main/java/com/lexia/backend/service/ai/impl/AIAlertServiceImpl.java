package com.lexia.backend.service.ai.impl;

import com.lexia.backend.entity.AIAlert;
import com.lexia.backend.repository.AIAlertRepository;
import com.lexia.backend.service.ai.AIAlertService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
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
    public Page<AIAlert> getAlerts(Boolean isRead, String severity, String type, Pageable pageable) {
        Specification<AIAlert> spec = Specification.where(null);

        if (isRead != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("isRead"), isRead));
        }
        if (severity != null && !severity.isEmpty()) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("severity"), severity));
        }
        if (type != null && !type.isEmpty()) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("alertType"), type));
        }

        return alertRepository.findAll(spec, pageable);
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
    public void markAllAsRead() {
        alertRepository.markAllAsRead();
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

    @Override
    public boolean hasRecentAlert(String type, String severity, Duration duration) {
        Instant since = Instant.now().minus(duration);
        return alertRepository.existsByAlertTypeAndSeverityAndCreatedAtAfter(type, severity, since);
    }
}
