package com.lexia.backend.controller;

import com.lexia.backend.dto.speaking.SpeakingAssessmentResponseDTO;
import com.lexia.backend.entity.User;
import com.lexia.backend.service.ai.SpeakingAssessmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * REST Controller for Speaking lesson assessment.
 * Provides AI-powered speech recognition, pronunciation scoring, and feedback.
 * 
 * @author LEXIA Team
 * @since Sprint 6
 */
@RestController
@RequestMapping("/api/v1/lessons/{lessonId}/speaking")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Speaking Assessment", description = "AI-powered speaking lesson assessment")
@SecurityRequirement(name = "bearerAuth")
public class SpeakingAssessmentController {

        private final SpeakingAssessmentService speakingAssessmentService;

        /**
         * Assesses a user's speaking attempt for a specific prompt.
         * 
         * @param lessonId The Speaking lesson ID
         * @param promptId The prompt index (0-based)
         * @param audio    The audio recording (MP3, WAV, WebM, M4A)
         * @param user     The authenticated user
         * @return Complete assessment with scores and feedback
         */
        @PostMapping(value = "/assess/{promptId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
        @Operation(summary = "Assess speaking attempt", description = """
                        Submits an audio recording for AI-powered assessment.

                        The assessment includes:
                        - **Transcription**: Speech-to-text conversion
                        - **Pronunciation Score** (0-100): Accuracy (40%), Stress (30%), Fluency (30%)
                        - **Grammar Feedback**: Analysis of target grammar structures
                        - **Vocabulary Feedback**: Analysis of target vocabulary usage
                        - **Overall Score**: Combined weighted score

                        Supported audio formats: MP3, WAV, WebM, M4A (max 10MB)
                        """)
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Assessment completed successfully", content = @Content(schema = @Schema(implementation = SpeakingAssessmentResponseDTO.class))),
                        @ApiResponse(responseCode = "400", description = "Invalid audio format or request"),
                        @ApiResponse(responseCode = "401", description = "Authentication required"),
                        @ApiResponse(responseCode = "404", description = "Lesson or prompt not found"),
                        @ApiResponse(responseCode = "503", description = "AI service unavailable")
        })
        public ResponseEntity<SpeakingAssessmentResponseDTO> assessSpeaking(
                        @Parameter(description = "Speaking lesson ID", required = true) @PathVariable Long lessonId,

                        @Parameter(description = "Prompt index (0-based)", required = true) @PathVariable String promptId,

                        @Parameter(description = "Audio recording file", required = true) @RequestParam("audio") MultipartFile audio,

                        @AuthenticationPrincipal User user) {

                log.info("Received speaking assessment request: lesson={}, prompt={}, user={}, audioSize={}",
                                lessonId, promptId, user.getId(), audio != null ? audio.getSize() : 0);

                // Validate audio file
                if (audio == null || audio.isEmpty()) {
                        throw new IllegalArgumentException("Audio file is required");
                }

                // Validate file size (max 10MB)
                if (audio.getSize() > 10 * 1024 * 1024) {
                        throw new IllegalArgumentException("Audio file too large. Maximum size is 10MB");
                }

                // Validate content type
                String contentType = audio.getContentType();
                if (contentType != null && !isValidAudioType(contentType)) {
                        throw new IllegalArgumentException(
                                        "Unsupported audio format: " + contentType +
                                                        ". Supported: MP3, WAV, WebM, M4A");
                }

                SpeakingAssessmentResponseDTO response = speakingAssessmentService.assessSpeaking(
                                lessonId, promptId, audio, user.getId());

                return ResponseEntity.ok(response);
        }

        /**
         * Checks if the content type is a valid audio format.
         */
        private boolean isValidAudioType(String contentType) {
                return contentType.startsWith("audio/")
                                || contentType.equals("video/webm"); // WebM can contain audio
        }
}
