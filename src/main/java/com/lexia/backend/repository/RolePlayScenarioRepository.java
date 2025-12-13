package com.lexia.backend.repository;

import com.lexia.backend.entity.RolePlayScenario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository for RolePlayScenario entity.
 */
@Repository
public interface RolePlayScenarioRepository extends JpaRepository<RolePlayScenario, UUID>, JpaSpecificationExecutor<RolePlayScenario> {
    
    List<RolePlayScenario> findByCefrLevelAndDomain(String cefrLevel, String domain);
    
    List<RolePlayScenario> findByIsFallbackTrue();
    
    Page<RolePlayScenario> findByCefrLevel(String cefrLevel, Pageable pageable);
}
