package com.lexia.backend.repository;

import com.lexia.backend.entity.AIAlert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface AIAlertRepository extends JpaRepository<AIAlert, Long>, JpaSpecificationExecutor<AIAlert> {
    List<AIAlert> findByIsReadFalseOrderByCreatedAtDesc();

    boolean existsByAlertTypeAndSeverityAndCreatedAtAfter(String alertType, String severity, Instant createdAt);

    @Modifying
    @Query("UPDATE AIAlert a SET a.isRead = true WHERE a.isRead = false")
    void markAllAsRead();
}
