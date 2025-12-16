package com.lexia.backend.repository;

import com.lexia.backend.entity.AIConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AIConfigRepository extends JpaRepository<AIConfig, String> {
}
