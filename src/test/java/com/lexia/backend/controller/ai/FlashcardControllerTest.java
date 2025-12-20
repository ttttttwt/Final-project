package com.lexia.backend.controller.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lexia.backend.auth.CustomUserDetailsService;
import com.lexia.backend.auth.JwtTokenProvider;
import com.lexia.backend.dto.ai.*;
import com.lexia.backend.entity.User;
import com.lexia.backend.exception.ResourceNotFoundException;
import com.lexia.backend.service.ai.FlashcardService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for FlashcardController.
 * Tests all REST endpoints for flashcard operations.
 * 
 * @author LEXIA Team
 * @since Sprint 5
 */
@WebMvcTest(FlashcardController.class)
@AutoConfigureMockMvc(addFilters = false) // Disable security filters for unit testing
class FlashcardControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @MockitoBean
        private FlashcardService flashcardService;

        @MockitoBean
        private JwtTokenProvider jwtTokenProvider;

        @MockitoBean
        private CustomUserDetailsService customUserDetailsService;

        @MockitoBean
        private com.lexia.backend.auth.JwtAuthFilter jwtAuthFilter;

        @MockitoBean
        private com.lexia.backend.filter.UserActivityFilter userActivityFilter;

        private User testUser;
        private UUID testUserId;
        private UUID testDeckId;
        private FlashcardDeckDTO testDeck;
        private FlashcardStudySessionDTO testSession;

        @BeforeEach
        void setUp() {
                testUserId = UUID.randomUUID();
                testDeckId = UUID.randomUUID();

                testUser = new User();
                testUser.setId(testUserId);
                testUser.setEmail("test@example.com");

                // Set up test deck DTO
                testDeck = FlashcardDeckDTO.builder()
                                .id(testDeckId)
                                .userId(testUserId)
                                .title("Test Flashcard Deck")
                                .description("A test deck for unit testing")
                                .sourceType("USER_CREATED")
                                .cefrLevel("B2")
                                .cardCount(10)
                                .cards(List.of(
                                                FlashcardCardDTO.builder()
                                                                .front("agenda")
                                                                .back(FlashcardBackDTO.builder()
                                                                                .definition("a list of topics to be discussed")
                                                                                .exampleSentence(
                                                                                                "Let's review the agenda before the meeting.")
                                                                                .pronunciation("/əˈdʒendə/")
                                                                                .build())
                                                                .build()))
                                .createdAt(Instant.now())
                                .updatedAt(Instant.now())
                                .build();

                // Set up test study session
                testSession = FlashcardStudySessionDTO.builder()
                                .deckId(testDeckId)
                                .deckTitle("Test Flashcard Deck")
                                .sessionStartedAt(Instant.now())
                                .totalCards(10)
                                .dueCards(5)
                                .newCards(2)
                                .sessionSize(7)
                                .cardsToStudy(Collections.emptyList())
                                .stats(FlashcardStudySessionDTO.DeckStatsDTO.builder()
                                                .newCount(2)
                                                .learningCount(3)
                                                .reviewingCount(3)
                                                .masteredCount(2)
                                                .build())
                                .build();

                // Set up Spring Security context
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(testUser,
                                null, Collections.emptyList());
                SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        // ========== Generation Endpoint Tests ==========

        @Nested
        @DisplayName("POST /api/v1/ai/flashcards/generate")
        class GenerateFromLessonTests {

                @Test
                @DisplayName("Should generate flashcards from lesson successfully")
                void generateFromLesson_Success() throws Exception {
                        GenerateFlashcardsDTO request = GenerateFlashcardsDTO.builder()
                                        .lessonId(1L)
                                        .maxCards(20)
                                        .cefrLevel("B2")
                                        .includeExamples(true)
                                        .includePronunciation(true)
                                        .build();

                        FlashcardDeckDTO generatedDeck = FlashcardDeckDTO.builder()
                                        .id(testDeckId)
                                        .userId(testUserId)
                                        .title("Lesson 1: Business Basics")
                                        .sourceType("LESSON")
                                        .sourceId(1L)
                                        .cefrLevel("B2")
                                        .cardCount(20)
                                        .createdAt(Instant.now())
                                        .build();

                        when(flashcardService.generateFromLesson(any(GenerateFlashcardsDTO.class), eq(testUserId)))
                                        .thenReturn(generatedDeck);

                        mockMvc.perform(post("/api/v1/ai/flashcards/generate")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request)))
                                        .andExpect(status().isCreated())
                                        .andExpect(jsonPath("$.id").value(testDeckId.toString()))
                                        .andExpect(jsonPath("$.sourceType").value("LESSON"))
                                        .andExpect(jsonPath("$.sourceId").value(1))
                                        .andExpect(jsonPath("$.cardCount").value(20));

                        verify(flashcardService).generateFromLesson(any(GenerateFlashcardsDTO.class), eq(testUserId));
                }

                @Test
                @DisplayName("Should return 400 when lesson ID is missing")
                void generateFromLesson_MissingLessonId() throws Exception {
                        String invalidRequest = "{}";

                        mockMvc.perform(post("/api/v1/ai/flashcards/generate")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(invalidRequest))
                                        .andExpect(status().isBadRequest());
                }

                @Test
                @DisplayName("Should return 404 when lesson not found")
                void generateFromLesson_LessonNotFound() throws Exception {
                        GenerateFlashcardsDTO request = GenerateFlashcardsDTO.builder()
                                        .lessonId(999L)
                                        .build();

                        when(flashcardService.generateFromLesson(any(GenerateFlashcardsDTO.class), eq(testUserId)))
                                        .thenThrow(new ResourceNotFoundException("Lesson", 999L));

                        mockMvc.perform(post("/api/v1/ai/flashcards/generate")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request)))
                                        .andExpect(status().isNotFound());
                }
        }

        // ========== Deck CRUD Tests ==========

        @Nested
        @DisplayName("POST /api/v1/ai/flashcards/decks")
        class CreateDeckTests {

                @Test
                @DisplayName("Should create deck successfully")
                void createDeck_Success() throws Exception {
                        CreateFlashcardDeckDTO request = CreateFlashcardDeckDTO.builder()
                                        .title("My Custom Deck")
                                        .description("A manually created deck")
                                        .sourceType("USER_CREATED")
                                        .cefrLevel("B2")
                                        .cards(List.of(
                                                        FlashcardCardDTO.builder()
                                                                        .front("test")
                                                                        .back(FlashcardBackDTO.builder()
                                                                                        .definition("a procedure to check quality")
                                                                                        .build())
                                                                        .build()))
                                        .build();

                        when(flashcardService.createDeck(any(CreateFlashcardDeckDTO.class), eq(testUserId)))
                                        .thenReturn(testDeck);

                        mockMvc.perform(post("/api/v1/ai/flashcards/decks")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request)))
                                        .andExpect(status().isCreated())
                                        .andExpect(jsonPath("$.id").value(testDeckId.toString()))
                                        .andExpect(jsonPath("$.title").value("Test Flashcard Deck"));

                        verify(flashcardService).createDeck(any(CreateFlashcardDeckDTO.class), eq(testUserId));
                }

                @Test
                @DisplayName("Should return 400 when title is missing")
                void createDeck_MissingTitle() throws Exception {
                        String invalidRequest = "{\"sourceType\": \"USER_CREATED\"}";

                        mockMvc.perform(post("/api/v1/ai/flashcards/decks")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(invalidRequest))
                                        .andExpect(status().isBadRequest());
                }
        }

        @Nested
        @DisplayName("GET /api/v1/ai/flashcards/decks")
        class GetUserDecksTests {

                @Test
                @DisplayName("Should return paginated deck list")
                void getUserDecks_Success() throws Exception {
                        Page<FlashcardDeckDTO> deckPage = new PageImpl<>(
                                        List.of(testDeck),
                                        org.springframework.data.domain.PageRequest.of(0, 20),
                                        1);

                        when(flashcardService.getUserDecks(eq(testUserId), any(Pageable.class)))
                                        .thenReturn(deckPage);

                        mockMvc.perform(get("/api/v1/ai/flashcards/decks")
                                        .param("page", "0")
                                        .param("size", "20")
                                        .contentType(MediaType.APPLICATION_JSON))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.content", hasSize(1)))
                                        .andExpect(jsonPath("$.content[0].id").value(testDeckId.toString()))
                                        .andExpect(jsonPath("$.totalElements").value(1));

                        verify(flashcardService).getUserDecks(eq(testUserId), any(Pageable.class));
                }

                @Test
                @DisplayName("Should cap page size at 100")
                void getUserDecks_CapPageSize() throws Exception {
                        Page<FlashcardDeckDTO> deckPage = new PageImpl<>(List.of(),
                                        org.springframework.data.domain.PageRequest.of(0, 100), 0);

                        when(flashcardService.getUserDecks(eq(testUserId), any(Pageable.class)))
                                        .thenReturn(deckPage);

                        mockMvc.perform(get("/api/v1/ai/flashcards/decks")
                                        .param("size", "200") // Request 200, should be capped to 100
                                        .contentType(MediaType.APPLICATION_JSON))
                                        .andExpect(status().isOk());

                        // Verify the service was called with capped page size
                        verify(flashcardService).getUserDecks(eq(testUserId),
                                        argThat(pageable -> pageable.getPageSize() <= 100));
                }
        }

        @Nested
        @DisplayName("GET /api/v1/ai/flashcards/decks/{deckId}")
        class GetDeckTests {

                @Test
                @DisplayName("Should return deck with cards")
                void getDeck_Success() throws Exception {
                        when(flashcardService.getDeck(eq(testDeckId), eq(testUserId)))
                                        .thenReturn(testDeck);

                        mockMvc.perform(get("/api/v1/ai/flashcards/decks/{deckId}", testDeckId)
                                        .contentType(MediaType.APPLICATION_JSON))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.id").value(testDeckId.toString()))
                                        .andExpect(jsonPath("$.title").value("Test Flashcard Deck"))
                                        .andExpect(jsonPath("$.cards", hasSize(1)))
                                        .andExpect(jsonPath("$.cards[0].front").value("agenda"));

                        verify(flashcardService).getDeck(testDeckId, testUserId);
                }

                @Test
                @DisplayName("Should return 404 when deck not found")
                void getDeck_NotFound() throws Exception {
                        UUID nonExistentId = UUID.randomUUID();

                        when(flashcardService.getDeck(eq(nonExistentId), eq(testUserId)))
                                        .thenThrow(new ResourceNotFoundException("FlashcardDeck", nonExistentId));

                        mockMvc.perform(get("/api/v1/ai/flashcards/decks/{deckId}", nonExistentId)
                                        .contentType(MediaType.APPLICATION_JSON))
                                        .andExpect(status().isNotFound());
                }
        }

        @Nested
        @DisplayName("PUT /api/v1/ai/flashcards/decks/{deckId}")
        class UpdateDeckTests {

                @Test
                @DisplayName("Should update deck successfully")
                void updateDeck_Success() throws Exception {
                        UpdateFlashcardDeckDTO request = UpdateFlashcardDeckDTO.builder()
                                        .title("Updated Title")
                                        .description("Updated description")
                                        .build();

                        FlashcardDeckDTO updatedDeck = FlashcardDeckDTO.builder()
                                        .id(testDeckId)
                                        .userId(testUserId)
                                        .title("Updated Title")
                                        .description("Updated description")
                                        .sourceType("USER_CREATED")
                                        .cefrLevel("B2")
                                        .cardCount(10)
                                        .updatedAt(Instant.now())
                                        .build();

                        when(flashcardService.updateDeck(eq(testDeckId), any(UpdateFlashcardDeckDTO.class),
                                        eq(testUserId)))
                                        .thenReturn(updatedDeck);

                        mockMvc.perform(put("/api/v1/ai/flashcards/decks/{deckId}", testDeckId)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request)))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.title").value("Updated Title"))
                                        .andExpect(jsonPath("$.description").value("Updated description"));

                        verify(flashcardService).updateDeck(eq(testDeckId), any(UpdateFlashcardDeckDTO.class),
                                        eq(testUserId));
                }
        }

        @Nested
        @DisplayName("DELETE /api/v1/ai/flashcards/decks/{deckId}")
        class DeleteDeckTests {

                @Test
                @DisplayName("Should delete deck successfully")
                void deleteDeck_Success() throws Exception {
                        doNothing().when(flashcardService).deleteDeck(testDeckId, testUserId);

                        mockMvc.perform(delete("/api/v1/ai/flashcards/decks/{deckId}", testDeckId))
                                        .andExpect(status().isNoContent());

                        verify(flashcardService).deleteDeck(testDeckId, testUserId);
                }

                @Test
                @DisplayName("Should return 404 when deleting non-existent deck")
                void deleteDeck_NotFound() throws Exception {
                        UUID nonExistentId = UUID.randomUUID();

                        doThrow(new ResourceNotFoundException("FlashcardDeck", nonExistentId))
                                        .when(flashcardService).deleteDeck(nonExistentId, testUserId);

                        mockMvc.perform(delete("/api/v1/ai/flashcards/decks/{deckId}", nonExistentId))
                                        .andExpect(status().isNotFound());
                }
        }

        // ========== Study Session Tests ==========

        @Nested
        @DisplayName("GET /api/v1/ai/flashcards/decks/{deckId}/study")
        class GetStudySessionTests {

                @Test
                @DisplayName("Should return study session with due cards")
                void getStudySession_Success() throws Exception {
                        when(flashcardService.getStudySession(eq(testDeckId), eq(testUserId), eq(20)))
                                        .thenReturn(testSession);

                        mockMvc.perform(get("/api/v1/ai/flashcards/decks/{deckId}/study", testDeckId)
                                        .contentType(MediaType.APPLICATION_JSON))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.deckId").value(testDeckId.toString()))
                                        .andExpect(jsonPath("$.totalCards").value(10))
                                        .andExpect(jsonPath("$.dueCards").value(5))
                                        .andExpect(jsonPath("$.newCards").value(2))
                                        .andExpect(jsonPath("$.stats.masteredCount").value(2));

                        verify(flashcardService).getStudySession(testDeckId, testUserId, 20);
                }

                @Test
                @DisplayName("Should cap maxCards at 50")
                void getStudySession_CapMaxCards() throws Exception {
                        when(flashcardService.getStudySession(eq(testDeckId), eq(testUserId), eq(50)))
                                        .thenReturn(testSession);

                        mockMvc.perform(get("/api/v1/ai/flashcards/decks/{deckId}/study", testDeckId)
                                        .param("maxCards", "100") // Request 100, should be capped to 50
                                        .contentType(MediaType.APPLICATION_JSON))
                                        .andExpect(status().isOk());

                        verify(flashcardService).getStudySession(testDeckId, testUserId, 50);
                }
        }

        @Nested
        @DisplayName("POST /api/v1/ai/flashcards/decks/{deckId}/review")
        class SubmitReviewTests {

                @Test
                @DisplayName("Should submit review results successfully")
                void submitReview_Success() throws Exception {
                        FlashcardReviewResultDTO request = FlashcardReviewResultDTO.builder()
                                        .sessionTimeMs(120000L)
                                        .reviews(List.of(
                                                        FlashcardReviewResultDTO.CardReviewDTO.builder()
                                                                        .cardIndex(0)
                                                                        .quality(4)
                                                                        .timeSpentMs(5000L)
                                                                        .build(),
                                                        FlashcardReviewResultDTO.CardReviewDTO.builder()
                                                                        .cardIndex(1)
                                                                        .quality(5)
                                                                        .timeSpentMs(3000L)
                                                                        .build()))
                                        .build();

                        FlashcardStudySessionDTO updatedSession = FlashcardStudySessionDTO.builder()
                                        .deckId(testDeckId)
                                        .deckTitle("Test Flashcard Deck")
                                        .totalCards(10)
                                        .dueCards(3)
                                        .newCards(0)
                                        .build();

                        when(flashcardService.submitReview(eq(testDeckId), eq(testUserId),
                                        any(FlashcardReviewResultDTO.class)))
                                        .thenReturn(updatedSession);

                        mockMvc.perform(post("/api/v1/ai/flashcards/decks/{deckId}/review", testDeckId)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request)))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.deckId").value(testDeckId.toString()))
                                        .andExpect(jsonPath("$.dueCards").value(3));

                        verify(flashcardService).submitReview(eq(testDeckId), eq(testUserId),
                                        any(FlashcardReviewResultDTO.class));
                }

                @Test
                @DisplayName("Should return 400 when reviews list is missing")
                void submitReview_MissingReviews() throws Exception {
                        String invalidRequest = "{\"sessionTimeMs\": 120000}";

                        mockMvc.perform(post("/api/v1/ai/flashcards/decks/{deckId}/review", testDeckId)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(invalidRequest))
                                        .andExpect(status().isBadRequest());
                }
        }

        // ========== Helper Endpoint Tests ==========

        @Nested
        @DisplayName("GET /api/v1/ai/flashcards/due-count")
        class GetTotalDueCountTests {

                @Test
                @DisplayName("Should return total due cards count")
                void getTotalDueCount_Success() throws Exception {
                        when(flashcardService.getTotalDueCardCount(testUserId)).thenReturn(42L);

                        mockMvc.perform(get("/api/v1/ai/flashcards/due-count")
                                        .contentType(MediaType.APPLICATION_JSON))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.totalDueCards").value(42));

                        verify(flashcardService).getTotalDueCardCount(testUserId);
                }
        }

        @Nested
        @DisplayName("GET /api/v1/ai/flashcards/lessons/{lessonId}/deck")
        class CheckLessonDeckTests {

                @Test
                @DisplayName("Should return exists=true when deck exists")
                void checkLessonDeck_Exists() throws Exception {
                        when(flashcardService.getLessonDeck(eq(1L), eq(testUserId)))
                                        .thenReturn(testDeck);

                        mockMvc.perform(get("/api/v1/ai/flashcards/lessons/{lessonId}/deck", 1L)
                                        .contentType(MediaType.APPLICATION_JSON))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.exists").value(true))
                                        .andExpect(jsonPath("$.deckId").value(testDeckId.toString()));

                        verify(flashcardService).getLessonDeck(1L, testUserId);
                }

                @Test
                @DisplayName("Should return exists=false when deck does not exist")
                void checkLessonDeck_NotExists() throws Exception {
                        when(flashcardService.getLessonDeck(eq(99L), eq(testUserId)))
                                        .thenReturn(null);

                        mockMvc.perform(get("/api/v1/ai/flashcards/lessons/{lessonId}/deck", 99L)
                                        .contentType(MediaType.APPLICATION_JSON))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.exists").value(false))
                                        .andExpect(jsonPath("$.deckId").isEmpty());

                        verify(flashcardService).getLessonDeck(99L, testUserId);
                }
        }
}
