package com.lexia.backend.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Composite primary key class for LearningPathCourse entity.
 * 
 * <p>
 * This class represents the composite key consisting of learningPath and course.
 * Used with @IdClass annotation to define a composite primary key in JPA.
 * </p>
 * 
 * @author LEXIA Team
 * @since Sprint 2
 * @see LearningPathCourse
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LearningPathCourseId implements Serializable {

    /**
     * The learning path ID component of the composite key.
     */
    private Long learningPath;

    /**
     * The course ID component of the composite key.
     */
    private Long course;
}
