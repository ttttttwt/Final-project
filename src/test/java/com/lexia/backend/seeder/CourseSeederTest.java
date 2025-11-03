package com.lexia.backend.seeder;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lexia.backend.repository.CourseRepository;
import com.lexia.backend.repository.LessonRepository;
import com.lexia.backend.repository.SectionRepository;
import com.lexia.backend.service.LessonContentValidator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@SpringBootTest
@ActiveProfiles("dev")
@Transactional
class CourseSeederTest {

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private SectionRepository sectionRepository;

    @Autowired
    private LessonRepository lessonRepository;

    @Autowired
    private LessonContentValidator lessonContentValidator;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void whenApplicationStarts_thenDataIsSeededCorrectly() {
        // The seeder runs automatically on application context load with 'dev' profile

        // Verify counts
        assertEquals(3, courseRepository.count(), "Should seed 3 courses");
        assertEquals(6, sectionRepository.count(), "Should seed 6 sections");
        assertEquals(18, lessonRepository.count(), "Should seed 18 lessons");

        // Verify JSONB content is valid
        lessonRepository.findAll().forEach(lesson -> {
            assertDoesNotThrow(() -> {
                JsonNode contentNode = objectMapper.readTree(lesson.getContent());
                lessonContentValidator.validate(lesson.getLessonType(), contentNode);
            }, "Validation failed for lesson ID " + lesson.getId() + " with type " + lesson.getLessonType());
        });
    }
}
