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
        @Query("SELECT j FROM CustomMaterialJob j JOIN j.material m WHERE m.id = :materialId")
        Optional<CustomMaterialJob> findByMaterialId(@Param("materialId") UUID materialId);

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
         * Checks if job exists for material.
         * 
         * @param materialId the material ID
         * @return true if job exists
         */
        @Query("SELECT COUNT(j) > 0 FROM CustomMaterialJob j JOIN j.material m WHERE m.id = :materialId")
        boolean existsByMaterialId(@Param("materialId") UUID materialId);

        /**
         * Deletes job by material ID.
         * 
         * @param materialId the material ID
         */
        @Modifying
        @Query("DELETE FROM CustomMaterialJob j WHERE j.material.id = :materialId")
        void deleteByMaterialId(@Param("materialId") UUID materialId);
}
