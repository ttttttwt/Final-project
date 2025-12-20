package com.lexia.backend.repository;

import com.lexia.backend.entity.UserCustomMaterialSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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
    @Query("SELECT s FROM UserCustomMaterialSettings s JOIN s.material m WHERE m.id = :materialId")
    Optional<UserCustomMaterialSettings> findByMaterialId(@Param("materialId") UUID materialId);

    /**
     * Checks if settings exist for a material.
     * 
     * @param materialId the material ID
     * @return true if settings exist
     */
    @Query("SELECT COUNT(s) > 0 FROM UserCustomMaterialSettings s JOIN s.material m WHERE m.id = :materialId")
    boolean existsByMaterialId(@Param("materialId") UUID materialId);

    /**
     * Deletes settings by material ID.
     * 
     * @param materialId the material ID
     */
    @Modifying
    @Query("DELETE FROM UserCustomMaterialSettings s WHERE s.material.id = :materialId")
    void deleteByMaterialId(@Param("materialId") UUID materialId);
}
