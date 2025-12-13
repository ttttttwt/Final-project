package com.lexia.backend.repository;

import com.lexia.backend.entity.RolePlayConversation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository for RolePlayConversation entity.
 */
@Repository
public interface RolePlayConversationRepository extends JpaRepository<RolePlayConversation, UUID>, JpaSpecificationExecutor<RolePlayConversation> {
    
    List<RolePlayConversation> findByUserId(UUID userId);
    
    Page<RolePlayConversation> findByUserId(UUID userId, Pageable pageable);
    
    List<RolePlayConversation> findByUserIdAndStatus(UUID userId, String status);
}
