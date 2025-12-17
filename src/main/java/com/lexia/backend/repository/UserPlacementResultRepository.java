package com.lexia.backend.repository;

import com.lexia.backend.entity.UserPlacementResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface UserPlacementResultRepository extends JpaRepository<UserPlacementResult, UUID> {
    List<UserPlacementResult> findByUserIdOrderByCreatedAtDesc(UUID userId);
}
