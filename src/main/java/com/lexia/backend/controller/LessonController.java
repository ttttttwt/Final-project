package com.lexia.backend.controller;

import com.lexia.backend.dto.CreateLessonDTO;
import com.lexia.backend.dto.LessonDTO;
import com.lexia.backend.dto.UpdateLessonDTO;
import com.lexia.backend.service.LessonService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Lesson Management.
 * Handles CRUD operations for lessons within course sections.
 * 
 * <p>
 * Base path: /api/v1/lessons
 * </p>
 * 
 * <p>
 * Security:
 * </p>
 * <ul>
 * <li>All endpoints require authentication</li>
 * <li>Create/Update/Delete operations require CONTENT_MANAGER role</li>
 * <li>Read operations available to all authenticated users</li>
 * </ul>
 * 
 * @author LEXIA Team
 * @since Sprint 2
 */
@RestController
@RequestMapping("/api/v1/lessons")
@Tag(name = "Lesson Management API", description = "Endpoints for managing lessons within course sections, including creation, updates, deletion, and retrieval with JSONB content")
@SecurityRequirement(name = "bearerAuth")
public class LessonController {

        private static final Logger LOG = LoggerFactory.getLogger(LessonController.class);

        private final LessonService lessonService;

        public LessonController(LessonService lessonService) {
                this.lessonService = lessonService;
        }

        /**
         * Get lesson by ID.
         * Available to all authenticated users.
         */
        @Operation(summary = "Get lesson by ID", description = "Retrieves detailed information about a specific lesson by its ID, including JSONB content. "
                        +
                        "Content structure varies by lesson type (READING, LISTENING, QUIZ, SPEAKING). " +
                        "See DATABASE-SCHEMA.md section 2.3 for content schemas.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Lesson found", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = LessonDTO.class), examples = @ExampleObject(name = "Success Response - READING Lesson", value = """
                                        {
                                          "id": 1,
                                          "sectionId": 1,
                                          "title": "Basic Greetings and Introductions",
                                          "lessonType": "READING",
                                          "content": "{\\"passages\\":[{\\"title\\":\\"Meeting People\\",\\"text\\":\\"When you meet someone new...\\"}],\\"questions\\":[{\\"question\\":\\"What should you say first?\\",\\"type\\":\\"multiple_choice\\",\\"options\\":[\\"Hello\\",\\"Goodbye\\",\\"Thank you\\"],\\"correctAnswer\\":0}]}",
                                          "orderIndex": 0,
                                          "durationMinutes": 15,
                                          "createdAt": "2025-10-30T10:15:30",
                                          "updatedAt": "2025-10-31T14:22:45"
                                        }
                                        """))),
                        @ApiResponse(responseCode = "404", description = "Lesson not found", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
                        @ApiResponse(responseCode = "401", description = "Unauthorized - invalid or missing token", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
        })
        @GetMapping("/{id}")
        public ResponseEntity<LessonDTO> getLessonById(
                        @Parameter(description = "Lesson ID", required = true, example = "1") @PathVariable Long id) {

                LOG.info("Fetching lesson by ID: {}", id);

                LessonDTO lesson = lessonService.getById(id);

                LOG.info("Retrieved lesson: {}", lesson.getTitle());

                return ResponseEntity.ok(lesson);
        }

        /**
         * Get all lessons for a specific section.
         * Available to all authenticated users.
         */
        @Operation(summary = "Get all lessons for a section", description = "Retrieves all lessons within a specific section, ordered by orderIndex. "
                        +
                        "Returns lessons with full JSONB content.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Lessons retrieved successfully", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = LessonDTO.class))),
                        @ApiResponse(responseCode = "404", description = "Section not found", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
                        @ApiResponse(responseCode = "401", description = "Unauthorized - invalid or missing token", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
        })
        @GetMapping("/sections/{sectionId}")
        public ResponseEntity<List<LessonDTO>> getLessonsBySectionId(
                        @Parameter(description = "Section ID", required = true, example = "1") @PathVariable Long sectionId) {

                LOG.info("Fetching lessons for section ID: {}", sectionId);

                List<LessonDTO> lessons = lessonService.getAllBySectionId(sectionId);

                LOG.info("Retrieved {} lessons for section ID: {}", lessons.size(), sectionId);

                return ResponseEntity.ok(lessons);
        }

        /**
         * Get all lessons for a specific course (across all sections).
         * Available to all authenticated users.
         */
        @Operation(summary = "Get all lessons for a course", description = "Retrieves all lessons within a specific course across all sections, "
                        +
                        "ordered by section and lesson orderIndex. Returns lessons with full JSONB content.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Lessons retrieved successfully", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = LessonDTO.class))),
                        @ApiResponse(responseCode = "401", description = "Unauthorized - invalid or missing token", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
        })
        @GetMapping("/courses/{courseId}")
        public ResponseEntity<List<LessonDTO>> getLessonsByCourseId(
                        @Parameter(description = "Course ID", required = true, example = "1") @PathVariable Long courseId) {

                LOG.info("Fetching lessons for course ID: {}", courseId);

                List<LessonDTO> lessons = lessonService.getAllByCourseId(courseId);

                LOG.info("Retrieved {} lessons for course ID: {}", lessons.size(), courseId);

                return ResponseEntity.ok(lessons);
        }

        /**
         * Create a new lesson within a section.
         * Requires CONTENT_MANAGER role.
         */
        @Operation(summary = "Create a new lesson", description = "Creates a new lesson within a specific section. " +
                        "Requires CONTENT_MANAGER role. JSONB content must be valid and match the lesson type schema. "
                        +
                        "See DATABASE-SCHEMA.md section 2.3 for content schemas. " +
                        "Content is validated by LessonContentValidator before creation.", requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Lesson creation data with JSONB content", required = true, content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CreateLessonDTO.class), examples = {
                                        @ExampleObject(name = "READING Lesson Example", value = """
                                                        {
                                                          "title": "Basic Greetings and Introductions",
                                                          "lessonType": "READING",
                                                          "content": "{\\"passages\\":[{\\"title\\":\\"Meeting People\\",\\"text\\":\\"When you meet someone new, it's important to make a good first impression. Start with a friendly greeting like 'Hello' or 'Hi'. Then introduce yourself by saying 'My name is...' or 'I'm...'. Don't forget to smile!\\"}],\\"questions\\":[{\\"question\\":\\"What should you say first when meeting someone?\\",\\"type\\":\\"multiple_choice\\",\\"options\\":[\\"Hello\\",\\"Goodbye\\",\\"Thank you\\",\\"Sorry\\"],\\"correctAnswer\\":0,\\"explanation\\":\\"A friendly greeting is the best way to start a conversation.\\"}],\\"vocabulary\\":[{\\"word\\":\\"greeting\\",\\"definition\\":\\"A polite word or sign of welcome\\",\\"example\\":\\"She waved in greeting.\\"}]}",
                                                          "orderIndex": 0,
                                                          "durationMinutes": 15
                                                        }
                                                        """),
                                        @ExampleObject(name = "LISTENING Lesson Example", value = """
                                                        {
                                                          "title": "Understanding Daily Conversations",
                                                          "lessonType": "LISTENING",
                                                          "content": "{\\"audioUrl\\":\\"https://cdn.lexia.com/audio/conversation-01.mp3\\",\\"duration\\":120,\\"transcript\\":\\"A: Good morning! How are you today? B: I'm fine, thank you. And you?\\",\\"questions\\":[{\\"question\\":\\"How is person B feeling?\\",\\"type\\":\\"multiple_choice\\",\\"options\\":[\\"Fine\\",\\"Sad\\",\\"Angry\\",\\"Tired\\"],\\"correctAnswer\\":0,\\"timestamp\\":15}]}",
                                                          "orderIndex": 1,
                                                          "durationMinutes": 20
                                                        }
                                                        """),
                                        @ExampleObject(name = "QUIZ Lesson Example", value = """
                                                        {
                                                          "title": "Grammar Quiz: Present Tense",
                                                          "lessonType": "QUIZ",
                                                          "content": "{\\"title\\":\\"Present Simple Tense\\",\\"description\\":\\"Test your knowledge of present simple tense\\",\\"questions\\":[{\\"question\\":\\"She ___ to school every day.\\",\\"type\\":\\"multiple_choice\\",\\"options\\":[\\"go\\",\\"goes\\",\\"going\\",\\"gone\\"],\\"correctAnswer\\":1,\\"points\\":10}],\\"passingScore\\":70}",
                                                          "orderIndex": 2,
                                                          "durationMinutes": 10
                                                        }
                                                        """),
                                        @ExampleObject(name = "SPEAKING Lesson Example", value = """
                                                        {
                                                          "title": "Restaurant Conversation Practice",
                                                          "lessonType": "SPEAKING",
                                                          "content": "{\\"scenario\\":\\"You are ordering food at a restaurant\\",\\"difficulty\\":\\"intermediate\\",\\"turns\\":5,\\"prompts\\":[\\"Greet the waiter\\",\\"Order a drink\\",\\"Ask about the menu\\",\\"Place your order\\",\\"Ask for the bill\\"],\\"sampleAnswers\\":[\\"Good evening!\\",\\"I'd like a glass of water, please.\\",\\"What do you recommend?\\",\\"I'll have the pasta, please.\\",\\"Can I get the check, please?\\"]}",
                                                          "orderIndex": 3,
                                                          "durationMinutes": 25
                                                        }
                                                        """)
                        })))
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "201", description = "Lesson created successfully", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = LessonDTO.class))),
                        @ApiResponse(responseCode = "400", description = "Invalid input data - validation errors or invalid JSONB content", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = @ExampleObject(name = "Invalid Content Error", value = """
                                        {
                                          "timestamp": "2025-10-31T14:22:45",
                                          "status": 400,
                                          "error": "Invalid Lesson Content",
                                          "message": "READING lesson validation failed: passages field is required",
                                          "path": "/api/v1/lessons/sections/1/lessons"
                                        }
                                        """))),
                        @ApiResponse(responseCode = "401", description = "Unauthorized - invalid or missing token", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
                        @ApiResponse(responseCode = "403", description = "Forbidden - requires CONTENT_MANAGER or ADMIN role", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
                        @ApiResponse(responseCode = "404", description = "Section not found", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
        })
        @PostMapping("/sections/{sectionId}/lessons")
        @PreAuthorize("hasAnyRole('ADMIN', 'CONTENT_MANAGER')")
        public ResponseEntity<LessonDTO> createLesson(
                        @Parameter(description = "Section ID where the lesson will be created", required = true, example = "1") @PathVariable Long sectionId,
                        @Valid @RequestBody CreateLessonDTO dto) {

                LOG.info("Creating new lesson in section ID: {}, title: {}", sectionId, dto.getTitle());

                LessonDTO createdLesson = lessonService.create(sectionId, dto);

                LOG.info("Lesson created successfully with ID: {}", createdLesson.getId());

                return ResponseEntity.status(HttpStatus.CREATED).body(createdLesson);
        }

        /**
         * Update an existing lesson.
         * Requires CONTENT_MANAGER role.
         */
        @Operation(summary = "Update an existing lesson", description = "Updates lesson information. Only provided fields will be updated. "
                        +
                        "Requires CONTENT_MANAGER role. If updating content, it must be valid JSON matching the lesson type schema. "
                        +
                        "Content is validated by LessonContentValidator if changed.", requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Lesson update data (all fields optional)", required = true, content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = UpdateLessonDTO.class), examples = @ExampleObject(name = "Update Lesson Example", value = """
                                        {
                                          "title": "Advanced Greetings and Introductions",
                                          "durationMinutes": 20,
                                          "content": "{\\"passages\\":[{\\"title\\":\\"Professional Introductions\\",\\"text\\":\\"In business settings, introductions are more formal...\\"}],\\"questions\\":[{\\"question\\":\\"What is appropriate in a business setting?\\",\\"type\\":\\"multiple_choice\\",\\"options\\":[\\"Good morning, I'm John Smith\\",\\"Hey, what's up?\\",\\"Yo!\\"],\\"correctAnswer\\":0}]}"
                                        }
                                        """))))
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Lesson updated successfully", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = LessonDTO.class))),
                        @ApiResponse(responseCode = "400", description = "Invalid input data - validation errors or invalid JSONB content", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
                        @ApiResponse(responseCode = "401", description = "Unauthorized - invalid or missing token", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
                        @ApiResponse(responseCode = "403", description = "Forbidden - requires CONTENT_MANAGER or ADMIN role", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
                        @ApiResponse(responseCode = "404", description = "Lesson not found", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
        })
        @PutMapping("/{id}")
        @PreAuthorize("hasAnyRole('ADMIN', 'CONTENT_MANAGER')")
        public ResponseEntity<LessonDTO> updateLesson(
                        @Parameter(description = "Lesson ID", required = true, example = "1") @PathVariable Long id,
                        @Valid @RequestBody UpdateLessonDTO dto) {

                LOG.info("Updating lesson ID: {}", id);

                LessonDTO updatedLesson = lessonService.update(id, dto);

                LOG.info("Lesson updated successfully: {}", updatedLesson.getTitle());

                return ResponseEntity.ok(updatedLesson);
        }

        /**
         * Delete a lesson.
         * Requires CONTENT_MANAGER role.
         */
        @Operation(summary = "Delete a lesson", description = "Deletes a lesson by ID. Requires CONTENT_MANAGER role. "
                        +
                        "This operation will cascade delete related data (e.g., lesson progress).")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "204", description = "Lesson deleted successfully", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
                        @ApiResponse(responseCode = "401", description = "Unauthorized - invalid or missing token", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
                        @ApiResponse(responseCode = "403", description = "Forbidden - requires CONTENT_MANAGER or ADMIN role", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
                        @ApiResponse(responseCode = "404", description = "Lesson not found", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
        })
        @DeleteMapping("/{id}")
        @PreAuthorize("hasAnyRole('ADMIN', 'CONTENT_MANAGER')")
        public ResponseEntity<Void> deleteLesson(
                        @Parameter(description = "Lesson ID", required = true, example = "1") @PathVariable Long id) {

                LOG.info("Deleting lesson ID: {}", id);

                lessonService.delete(id);

                LOG.info("Lesson deleted successfully: {}", id);

                return ResponseEntity.noContent().build();
        }

        /**
         * Reorder a lesson within its section.
         * Requires CONTENT_MANAGER role.
         */
        @Operation(summary = "Reorder a lesson", description = "Changes the order position of a lesson within its section. "
                        +
                        "Requires CONTENT_MANAGER role. Other lessons in the section will be reordered accordingly.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Lesson reordered successfully", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = LessonDTO.class))),
                        @ApiResponse(responseCode = "401", description = "Unauthorized - invalid or missing token", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
                        @ApiResponse(responseCode = "403", description = "Forbidden - requires CONTENT_MANAGER or ADMIN role", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
                        @ApiResponse(responseCode = "404", description = "Lesson not found", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
        })
        @PatchMapping("/{id}/reorder")
        @PreAuthorize("hasAnyRole('ADMIN', 'CONTENT_MANAGER')")
        public ResponseEntity<LessonDTO> reorderLesson(
                        @Parameter(description = "Lesson ID", required = true, example = "1") @PathVariable Long id,
                        @Parameter(description = "New order index (0-based)", required = true, example = "2") @RequestParam Integer newOrderIndex) {

                LOG.info("Reordering lesson ID: {} to position: {}", id, newOrderIndex);

                LessonDTO reorderedLesson = lessonService.reorder(id, newOrderIndex);

                LOG.info("Lesson reordered successfully: {}", reorderedLesson.getTitle());

                return ResponseEntity.ok(reorderedLesson);
        }
}
