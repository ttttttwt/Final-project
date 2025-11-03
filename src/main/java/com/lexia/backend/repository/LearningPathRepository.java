package com.lexia.backend.repository;

import com.lexia.backend.entity.LearningPath;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for LearningPath entity.
 * 
 * <p>
 * Provides CRUD operations and custom query methods for managing learning
 * paths.
 * </p>
 * 
 * @author LEXIA Team
 * @since Sprint 2
 * @see LearningPath
 */
@Repository
public interface LearningPathRepository extends JpaRepository<LearningPath, Long> {

    /**
     * Find all learning paths targeting a specific CEFR level.
     * 
     * @param cefrLevel the CEFR level (A1, A2, B1, B2, C1, C2)
     * @return list of learning paths for the given CEFR level
     */
    List<LearningPath> findByCefrLevel(String cefrLevel);

    /**
     * Find all default (system-provided) learning paths.
     * 
     * @return list of default learning paths
     */
    List<LearningPath> findByIsDefaultTrue();

    /**
     * Find a default learning path for a specific CEFR level.
     * 
     * @param cefrLevel the CEFR level (A1, A2, B1, B2, C1, C2)
     * @return Optional containing the default path if found
     */
    Optional<LearningPath> findByCefrLevelAndIsDefaultTrue(String cefrLevel);

    /**
     * Check if a learning path with the given name exists.
     * 
     * @param name the learning path name
     * @return true if a path with the name exists, false otherwise
     */
    boolean existsByName(String name);
}
