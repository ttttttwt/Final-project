package com.lexia.backend.repository;

import com.lexia.backend.entity.PromoCode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for PromoCode entity.
 */
@Repository
public interface PromoCodeRepository extends JpaRepository<PromoCode, UUID> {

    /**
     * Find by code (case-insensitive)
     */
    @Query("SELECT p FROM PromoCode p WHERE UPPER(p.code) = UPPER(:code)")
    Optional<PromoCode> findByCodeIgnoreCase(@Param("code") String code);

    /**
     * Find all active promo codes
     */
    Page<PromoCode> findByIsActiveTrueOrderByCreatedAtDesc(Pageable pageable);

    /**
     * Find currently valid promo codes
     */
    @Query("""
            SELECT p FROM PromoCode p
            WHERE p.isActive = true
            AND p.validFrom <= :now
            AND p.validUntil >= :now
            AND (p.maxUses IS NULL OR p.usedCount < p.maxUses)
            ORDER BY p.createdAt DESC
            """)
    List<PromoCode> findValidPromoCodes(@Param("now") LocalDateTime now);

    /**
     * Check if code exists (case-insensitive)
     */
    @Query("SELECT COUNT(p) > 0 FROM PromoCode p WHERE UPPER(p.code) = UPPER(:code)")
    boolean existsByCodeIgnoreCase(@Param("code") String code);

    /**
     * Count active promo codes
     */
    long countByIsActiveTrue();

    /**
     * Find expired promo codes
     */
    @Query("""
            SELECT p FROM PromoCode p
            WHERE p.isActive = true
            AND p.validUntil < :now
            """)
    List<PromoCode> findExpiredPromoCodes(@Param("now") LocalDateTime now);

    /**
     * Search promo codes by code or description
     */
    @Query("""
            SELECT p FROM PromoCode p
            WHERE LOWER(p.code) LIKE LOWER(CONCAT('%', :search, '%'))
            OR LOWER(p.description) LIKE LOWER(CONCAT('%', :search, '%'))
            ORDER BY p.createdAt DESC
            """)
    Page<PromoCode> searchPromoCodes(@Param("search") String search, Pageable pageable);
}
