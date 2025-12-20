package com.lexia.backend.repository;

import com.lexia.backend.entity.CustomMaterialJob;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for CustomMaterialJob entity.
 * 
 * @since Sprint 5
 */
@Repository
public interface CustomMaterialJobRepository extends JpaRepository<CustomMaterialJob, UUID> {

    /**
     * Finds job by material ID.
     * 
     * @param materialId the material ID
     * @return the job if found
     */
    Optional<CustomMaterialJob> findByMaterialId(UUID materialId);

    /**
     * Finds all queued jobs ordered by creation time (FIFO).
     * 
     * @param pageable pagination for batch processing
     * @return page of queued jobs
     */
    @Query("SELECT j FROM CustomMaterialJob j " +
            "WHERE j.status = 'QUEUED' " +
            "ORDER BY j.createdAt ASC")
    Page<CustomMaterialJob> findQueuedJobs(Pageable pageable);

    /**
     * Finds all jobs by status.
     * 
     * @param status the job status
     * @return list of jobs
     */
    List<CustomMaterialJob> findByStatusOrderByCreatedAtAsc(String status);

    /**
     * Finds stale processing jobs (jobs stuck in processing).
     * 
     * @param status   should be "PROCESSING"
     * @param pageable pagination info
     * @return page of stale jobs
     */
    @Query("SELECT j FROM CustomMaterialJob j " +
            "WHERE j.status = :status " +
            "AND j.startedAt < :staleThreshold")
    Page<CustomMaterialJob> findStaleJobs(
            @Param("status") String status,
            @Param("staleThreshold") java.time.Instant staleThreshold,
            Pageable pageable);

    /**
     * Counts jobs by status.
     * 
     * @param status the job status
     * @return count of jobs
     */
    long countByStatus(String status);

    /**
     * Updates job status atomically.
     * 
     * @param jobId         the job ID
     * @param currentStatus expected current status
     * @param newStatus     new status to set
     * @return number of rows updated (1 if success, 0 if status changed)
     */
    @Modifying
    @Query("UPDATE CustomMaterialJob j " +
            "SET j.status = :newStatus, j.updatedAt = CURRENT_TIMESTAMP " +
            "WHERE j.id = :jobId AND j.status = :currentStatus")
    int updateStatusAtomic(
            @Param("jobId") UUID jobId,
            @Param("currentStatus") String currentStatus,
            @Param("newStatus") String newStatus);

    /**
     * Checks if job exists for material.
     * 
     * @param materialId the material ID
     * @return true if job exists
     */
    boolean existsByMaterialId(UUID materialId);

    /**
     * Deletes job by material ID.
     * 
     * @param materialId the material ID
     */
    void deleteByMaterialId(UUID materialId);
}
