package com.lexia.backend.repository;

import com.lexia.backend.entity.UserCustomMaterialSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository for UserCustomMaterialSettings entity.
 * 
 * @since Sprint 5
 */
@Repository
public interface UserCustomMaterialSettingsRepository extends JpaRepository<UserCustomMaterialSettings, UUID> {

    /**
     * Finds settings by material ID.
     * 
     * @param materialId the material ID
     * @return the settings if found
     */
    Optional<UserCustomMaterialSettings> findByMaterialId(UUID materialId);

    /**
     * Checks if settings exist for a material.
     * 
     * @param materialId the material ID
     * @return true if settings exist
     */
    boolean existsByMaterialId(UUID materialId);

    /**
     * Deletes settings by material ID.
     * 
     * @param materialId the material ID
     */
    void deleteByMaterialId(UUID materialId);
}
