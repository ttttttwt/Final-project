package com.lexia.backend.file.repository;

import com.lexia.backend.file.entity.FileEntity;
import com.lexia.backend.file.enums.FileCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for file metadata operations.
 */
@Repository
public interface FileRepository extends JpaRepository<FileEntity, UUID> {

    /**
     * Find file by storage path.
     *
     * @param storagePath the relative storage path
     * @return optional file entity
     */
    Optional<FileEntity> findByStoragePath(String storagePath);

    /**
     * Find all files uploaded by a specific user.
     *
     * @param userId   the user ID
     * @param pageable pagination info
     * @return page of files
     */
    Page<FileEntity> findByUploadedById(UUID userId, Pageable pageable);

    /**
     * Find all files in a specific category.
     *
     * @param category the file category
     * @param pageable pagination info
     * @return page of files
     */
    Page<FileEntity> findByCategory(FileCategory category, Pageable pageable);

    /**
     * Find files by user and category.
     *
     * @param userId   the user ID
     * @param category the file category
     * @return list of files
     */
    List<FileEntity> findByUploadedByIdAndCategory(UUID userId, FileCategory category);

    /**
     * Check if a file with the given storage path exists.
     *
     * @param storagePath the storage path to check
     * @return true if file exists
     */
    boolean existsByStoragePath(String storagePath);

    /**
     * Get total storage used by a user.
     *
     * @param userId the user ID
     * @return total bytes used
     */
    @Query("SELECT COALESCE(SUM(f.fileSize), 0) FROM FileEntity f WHERE f.uploadedBy.id = :userId")
    Long getTotalStorageByUser(@Param("userId") UUID userId);

    /**
     * Find public files in a category.
     *
     * @param category the file category
     * @param pageable pagination info
     * @return page of public files
     */
    Page<FileEntity> findByCategoryAndIsPublicTrue(FileCategory category, Pageable pageable);
}
