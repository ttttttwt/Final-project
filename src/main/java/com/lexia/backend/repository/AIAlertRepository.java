package com.lexia.backend.repository;

import com.lexia.backend.entity.AIAlert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AIAlertRepository extends JpaRepository<AIAlert, Long> {
    List<AIAlert> findByIsReadFalseOrderByCreatedAtDesc();
}
