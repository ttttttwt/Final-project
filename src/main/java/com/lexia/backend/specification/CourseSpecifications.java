package com.lexia.backend.specification;

import com.lexia.backend.entity.Course;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * JPA Specifications for dynamic Course entity queries.
 * 
 * <p>
 * Provides type-safe, composable query criteria for filtering courses.
 * Specifications can be combined using AND/OR operators for complex searches.
 * </p>
 * 
 * <p>
 * <strong>Usage Example:</strong>
 * </p>
 * 
 * <pre>
 * Specification&lt;Course&gt; spec = Specification
 *         .where(CourseSpecifications.hasTitle("Business"))
 *         .and(CourseSpecifications.hasCefrLevel("B1"))
 *         .and(CourseSpecifications.isPublished(true));
 * 
 * List&lt;Course&gt; courses = courseRepository.findAll(spec);
 * </pre>
 * 
 * @author LEXIA Team
 * @since Sprint 2
 * @see Course
 * @see org.springframework.data.jpa.domain.Specification
 */
public class CourseSpecifications {

    /**
     * Private constructor to prevent instantiation.
     * This is a utility class with only static methods.
     */
    private CourseSpecifications() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Specification for filtering courses by title (case-insensitive, partial
     * match).
     * Uses ILIKE for PostgreSQL case-insensitive search.
     * 
     * <p>
     * If title is null or blank, returns a specification that matches all courses.
     * </p>
     * 
     * @param title the title keyword to search for (supports partial match)
     * @return specification that matches courses with the given title
     */
    public static Specification<Course> hasTitle(String title) {
        return (Root<Course> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> {
            if (title == null || title.isBlank()) {
                return cb.conjunction(); // Always true
            }
            return cb.like(cb.lower(root.get("title")), "%" + title.toLowerCase() + "%");
        };
    }

    /**
     * Specification for filtering courses by CEFR level.
     * 
     * <p>
     * If cefrLevel is null or blank, returns a specification that matches all
     * courses.
     * </p>
     * 
     * @param cefrLevel the CEFR level (A1, A2, B1, B2, C1, C2)
     * @return specification that matches courses at the given CEFR level
     */
    public static Specification<Course> hasCefrLevel(String cefrLevel) {
        return (Root<Course> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> {
            if (cefrLevel == null || cefrLevel.isBlank()) {
                return cb.conjunction(); // Always true
            }
            return cb.equal(root.get("cefrLevel"), cefrLevel);
        };
    }

    /**
     * Specification for filtering courses by publication status.
     * 
     * <p>
     * If isPublished is null, returns a specification that matches all courses.
     * </p>
     * 
     * @param isPublished the publication status (true for published, false for
     *                    unpublished)
     * @return specification that matches courses with the given publication status
     */
    public static Specification<Course> isPublished(Boolean isPublished) {
        return (Root<Course> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> {
            if (isPublished == null) {
                return cb.conjunction(); // Always true
            }
            return cb.equal(root.get("isPublished"), isPublished);
        };
    }

    /**
     * Specification for filtering courses created between two dates.
     * 
     * <p>
     * Both start and end dates are inclusive. If either is null, that bound is
     * ignored.
     * </p>
     * 
     * @param start the start date (inclusive), or null for no lower bound
     * @param end   the end date (inclusive), or null for no upper bound
     * @return specification that matches courses created within the date range
     */
    public static Specification<Course> createdBetween(LocalDateTime start, LocalDateTime end) {
        return (Root<Course> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (start != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), start));
            }

            if (end != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), end));
            }

            if (predicates.isEmpty()) {
                return cb.conjunction(); // Always true
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    /**
     * Specification for filtering courses created after a specific date.
     * 
     * @param date the date after which courses should have been created (inclusive)
     * @return specification that matches courses created on or after the given date
     */
    public static Specification<Course> createdAfter(LocalDateTime date) {
        return (Root<Course> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> {
            if (date == null) {
                return cb.conjunction(); // Always true
            }
            return cb.greaterThanOrEqualTo(root.get("createdAt"), date);
        };
    }

    /**
     * Specification for filtering courses created before a specific date.
     * 
     * @param date the date before which courses should have been created
     *             (inclusive)
     * @return specification that matches courses created on or before the given
     *         date
     */
    public static Specification<Course> createdBefore(LocalDateTime date) {
        return (Root<Course> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> {
            if (date == null) {
                return cb.conjunction(); // Always true
            }
            return cb.lessThanOrEqualTo(root.get("createdAt"), date);
        };
    }

    /**
     * Specification for filtering courses by description content (case-insensitive,
     * partial match).
     * 
     * <p>
     * If keyword is null or blank, returns a specification that matches all
     * courses.
     * </p>
     * 
     * @param keyword the keyword to search for in course descriptions
     * @return specification that matches courses with the given keyword in
     *         description
     */
    public static Specification<Course> descriptionContains(String keyword) {
        return (Root<Course> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> {
            if (keyword == null || keyword.isBlank()) {
                return cb.conjunction(); // Always true
            }
            return cb.like(cb.lower(root.get("description")), "%" + keyword.toLowerCase() + "%");
        };
    }

    /**
     * Composite specification combining multiple search criteria.
     * This is a convenience method for common search scenarios.
     * 
     * <p>
     * Combines title, CEFR level, and publication status filters using AND logic.
     * Null or blank values are ignored.
     * </p>
     * 
     * @param title       the title keyword to search for
     * @param cefrLevel   the CEFR level to filter by
     * @param isPublished the publication status to filter by
     * @return composite specification combining all provided criteria
     */
    public static Specification<Course> searchCourses(String title, String cefrLevel, Boolean isPublished) {
        return hasTitle(title)
                .and(hasCefrLevel(cefrLevel))
                .and(isPublished(isPublished));
    }

    /**
     * Advanced search specification with date range.
     * Combines title, CEFR level, publication status, and creation date filtering.
     * 
     * @param title         the title keyword to search for
     * @param cefrLevel     the CEFR level to filter by
     * @param isPublished   the publication status to filter by
     * @param createdAfter  courses created on or after this date
     * @param createdBefore courses created on or before this date
     * @return composite specification combining all provided criteria
     */
    public static Specification<Course> advancedSearch(
            String title,
            String cefrLevel,
            Boolean isPublished,
            LocalDateTime createdAfter,
            LocalDateTime createdBefore) {
        return hasTitle(title)
                .and(hasCefrLevel(cefrLevel))
                .and(isPublished(isPublished))
                .and(createdBetween(createdAfter, createdBefore));
    }
}
