package com.lexia.backend.service;

import com.lexia.backend.dto.LearningPathDTO;
import com.lexia.backend.dto.UserPathProgressDTO;
import com.lexia.backend.entity.*;
import com.lexia.backend.exception.LearningPathNotFoundException;
import com.lexia.backend.repository.LearningPathRepository;
import com.lexia.backend.repository.UserLearningPathRepository;
import com.lexia.backend.service.impl.LearningPathServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for LearningPathService.
 * Tests all learning path operations, recommendation logic, enrollment, and
 * progress tracking.
 * 
 * @author LEXIA Team
 * @since Sprint 2
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("LearningPathService Tests")
class LearningPathServiceTest {

    @Mock
    private LearningPathRepository learningPathRepository;

    @Mock
    private UserLearningPathRepository userLearningPathRepository;

    @InjectMocks
    private LearningPathServiceImpl learningPathService;

    private LearningPath validPath;
    private LearningPath pathWithCourses;
    private Course course1;
    private Course course2;
    private User userWithLevel;
    private User userWithoutLevel;
    private UserLearningPath userLearningPath;
    private LearningPathCourse pathCourse1;
    private LearningPathCourse pathCourse2;

    @BeforeEach
    void setUp() {
        // Setup courses
        course1 = Course.builder()
                .id(1L)
                .title("English Basics (A1)")
                .description("Beginner course")
                .cefrLevel("A1")
                .isPublished(true)
                .sections(new ArrayList<>())
                .build();

        Section section1 = Section.builder()
                .id(1L)
                .title("Section 1")
                .orderIndex(0)
                .course(course1)
                .lessons(new ArrayList<>())
                .build();
        course1.getSections().add(section1);

        course2 = Course.builder()
                .id(2L)
                .title("Intermediate English (B1)")
                .description("Intermediate course")
                .cefrLevel("B1")
                .isPublished(true)
                .sections(new ArrayList<>())
                .build();

        Section section2 = Section.builder()
                .id(2L)
                .title("Section 2")
                .orderIndex(0)
                .course(course2)
                .lessons(new ArrayList<>())
                .build();
        course2.getSections().add(section2);

        // Setup learning paths
        validPath = LearningPath.builder()
                .id(1L)
                .name("Beginner Path (A1)")
                .description("Complete beginners path")
                .cefrLevel("A1")
                .isDefault(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .learningPathCourses(new ArrayList<>())
                .userLearningPaths(new ArrayList<>())
                .build();

        pathWithCourses = LearningPath.builder()
                .id(2L)
                .name("Upper Intermediate Path (B2)")
                .description("B2 level path")
                .cefrLevel("B2")
                .isDefault(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .learningPathCourses(new ArrayList<>())
                .userLearningPaths(new ArrayList<>())
                .build();

        // Setup learning path courses
        pathCourse1 = LearningPathCourse.builder()
                .learningPath(pathWithCourses)
                .course(course1)
                .orderIndex(0)
                .build();

        pathCourse2 = LearningPathCourse.builder()
                .learningPath(pathWithCourses)
                .course(course2)
                .orderIndex(1)
                .build();

        pathWithCourses.getLearningPathCourses().add(pathCourse1);
        pathWithCourses.getLearningPathCourses().add(pathCourse2);

        // Setup users
        userWithLevel = User.builder()
                .id(UUID.randomUUID())
                .email("user@test.com")
                .build();

        UserProfile profileWithLevel = UserProfile.builder()
                .userId(userWithLevel.getId())
                .currentLevel("B2")
                .user(userWithLevel)
                .build();

        userWithLevel.setProfile(profileWithLevel);

        userWithoutLevel = User.builder()
                .id(UUID.randomUUID())
                .email("newuser@test.com")
                .profile(new UserProfile())
                .build();

        // Setup user learning path
        userLearningPath = UserLearningPath.builder()
                .id(1L)
                .user(userWithLevel)
                .learningPath(pathWithCourses)
                .currentCourse(course1)
                .startedAt(LocalDateTime.now())
                .build();
    }

    // ==================== getAllPaths() Tests ====================

    @Test
    @DisplayName("getAllPaths - Should return all learning paths")
    void testGetAllPaths_Success() {
        // Arrange
        List<LearningPath> paths = Arrays.asList(validPath, pathWithCourses);
        when(learningPathRepository.findAll()).thenReturn(paths);

        // Act
        List<LearningPathDTO> result = learningPathService.getAllPaths();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(learningPathRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("getAllPaths - Should return empty list when no paths exist")
    void testGetAllPaths_EmptyList() {
        // Arrange
        when(learningPathRepository.findAll()).thenReturn(new ArrayList<>());

        // Act
        List<LearningPathDTO> result = learningPathService.getAllPaths();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(learningPathRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("getAllPaths - Should include courses in result")
    void testGetAllPaths_IncludesCourses() {
        // Arrange
        List<LearningPath> paths = Arrays.asList(pathWithCourses);
        when(learningPathRepository.findAll()).thenReturn(paths);

        // Act
        List<LearningPathDTO> result = learningPathService.getAllPaths();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(2, result.get(0).getCourses().size());
        assertEquals(2, result.get(0).getTotalCourses());
        verify(learningPathRepository, times(1)).findAll();
    }

    // ==================== getPathById() Tests ====================

    @Test
    @DisplayName("getPathById - Should return path when found")
    void testGetPathById_Success() {
        // Arrange
        when(learningPathRepository.findById(1L)).thenReturn(Optional.of(validPath));

        // Act
        LearningPathDTO result = learningPathService.getPathById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Beginner Path (A1)", result.getName());
        assertEquals("A1", result.getCefrLevel());
        verify(learningPathRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("getPathById - Should throw exception when path not found")
    void testGetPathById_NotFound() {
        // Arrange
        when(learningPathRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        LearningPathNotFoundException exception = assertThrows(
                LearningPathNotFoundException.class,
                () -> learningPathService.getPathById(999L));

        assertTrue(exception.getMessage().contains("999"));
        verify(learningPathRepository, times(1)).findById(999L);
    }

    @Test
    @DisplayName("getPathById - Should include courses in result")
    void testGetPathById_IncludesCourses() {
        // Arrange
        when(learningPathRepository.findById(2L)).thenReturn(Optional.of(pathWithCourses));

        // Act
        LearningPathDTO result = learningPathService.getPathById(2L);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.getCourses().size());
        assertEquals(0, result.getCourses().get(0).getOrderIndex());
        assertEquals(1, result.getCourses().get(1).getOrderIndex());
        verify(learningPathRepository, times(1)).findById(2L);
    }

    // ==================== getRecommendedPath() Tests ====================

    @Test
    @DisplayName("getRecommendedPath - Should recommend path based on user's CEFR level")
    void testGetRecommendedPath_WithUserLevel() {
        // Arrange
        when(learningPathRepository.findByCefrLevelAndIsDefaultTrue("B2"))
                .thenReturn(Optional.of(pathWithCourses));

        // Act
        LearningPathDTO result = learningPathService.getRecommendedPath(userWithLevel);

        // Assert
        assertNotNull(result);
        assertEquals("B2", result.getCefrLevel());
        assertEquals("Upper Intermediate Path (B2)", result.getName());
        verify(learningPathRepository, times(1)).findByCefrLevelAndIsDefaultTrue("B2");
    }

    @Test
    @DisplayName("getRecommendedPath - Should default to A1 when user has no level")
    void testGetRecommendedPath_NoUserLevel() {
        // Arrange
        when(learningPathRepository.findByCefrLevelAndIsDefaultTrue("A1"))
                .thenReturn(Optional.of(validPath));

        // Act
        LearningPathDTO result = learningPathService.getRecommendedPath(userWithoutLevel);

        // Assert
        assertNotNull(result);
        assertEquals("A1", result.getCefrLevel());
        assertEquals("Beginner Path (A1)", result.getName());
        verify(learningPathRepository, times(1)).findByCefrLevelAndIsDefaultTrue("A1");
    }

    @Test
    @DisplayName("getRecommendedPath - Should default to A1 when user has empty level")
    void testGetRecommendedPath_EmptyUserLevel() {
        // Arrange
        userWithoutLevel.getProfile().setCurrentLevel("");
        when(learningPathRepository.findByCefrLevelAndIsDefaultTrue("A1"))
                .thenReturn(Optional.of(validPath));

        // Act
        LearningPathDTO result = learningPathService.getRecommendedPath(userWithoutLevel);

        // Assert
        assertNotNull(result);
        assertEquals("A1", result.getCefrLevel());
        verify(learningPathRepository, times(1)).findByCefrLevelAndIsDefaultTrue("A1");
    }

    @Test
    @DisplayName("getRecommendedPath - Should throw exception when no default path found")
    void testGetRecommendedPath_NoDefaultPathFound() {
        // Arrange
        when(learningPathRepository.findByCefrLevelAndIsDefaultTrue("B2"))
                .thenReturn(Optional.empty());

        // Act & Assert
        LearningPathNotFoundException exception = assertThrows(
                LearningPathNotFoundException.class,
                () -> learningPathService.getRecommendedPath(userWithLevel));

        assertTrue(exception.getMessage().contains("B2"));
        verify(learningPathRepository, times(1)).findByCefrLevelAndIsDefaultTrue("B2");
    }

    @Test
    @DisplayName("getRecommendedPath - Should handle user with null profile")
    void testGetRecommendedPath_NullProfile() {
        // Arrange
        User userWithNullProfile = User.builder()
                .id(UUID.randomUUID())
                .email("noprofile@test.com")
                .profile(null)
                .build();

        when(learningPathRepository.findByCefrLevelAndIsDefaultTrue("A1"))
                .thenReturn(Optional.of(validPath));

        // Act
        LearningPathDTO result = learningPathService.getRecommendedPath(userWithNullProfile);

        // Assert
        assertNotNull(result);
        assertEquals("A1", result.getCefrLevel());
        verify(learningPathRepository, times(1)).findByCefrLevelAndIsDefaultTrue("A1");
    }

    // ==================== startPath() Tests ====================

    @Test
    @DisplayName("startPath - Should successfully enroll user in path")
    void testStartPath_Success() {
        // Arrange
        when(learningPathRepository.findById(2L)).thenReturn(Optional.of(pathWithCourses));
        when(userLearningPathRepository.existsByUserIdAndPathId(userWithLevel.getId(), 2L))
                .thenReturn(false);
        when(userLearningPathRepository.save(any(UserLearningPath.class)))
                .thenReturn(userLearningPath);

        // Act
        UserPathProgressDTO result = learningPathService.startPath(userWithLevel, 2L);

        // Assert
        assertNotNull(result);
        assertEquals(2L, result.getPathId());
        assertEquals("Upper Intermediate Path (B2)", result.getPathName());
        assertEquals(1L, result.getCurrentCourseId());
        assertEquals("English Basics (A1)", result.getCurrentCourseTitle());
        assertEquals(0, result.getCoursesCompleted());
        assertEquals(2, result.getTotalCourses());
        assertFalse(result.getIsCompleted());
        verify(learningPathRepository, times(1)).findById(2L);
        verify(userLearningPathRepository, times(1)).existsByUserIdAndPathId(userWithLevel.getId(), 2L);
        verify(userLearningPathRepository, times(1)).save(any(UserLearningPath.class));
    }

    @Test
    @DisplayName("startPath - Should throw exception when path not found")
    void testStartPath_PathNotFound() {
        // Arrange
        when(learningPathRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        LearningPathNotFoundException exception = assertThrows(
                LearningPathNotFoundException.class,
                () -> learningPathService.startPath(userWithLevel, 999L));

        assertTrue(exception.getMessage().contains("999"));
        verify(learningPathRepository, times(1)).findById(999L);
        verify(userLearningPathRepository, never()).save(any());
    }

    @Test
    @DisplayName("startPath - Should throw exception when user already started path")
    void testStartPath_AlreadyStarted() {
        // Arrange
        when(learningPathRepository.findById(2L)).thenReturn(Optional.of(pathWithCourses));
        when(userLearningPathRepository.existsByUserIdAndPathId(userWithLevel.getId(), 2L))
                .thenReturn(true);

        // Act & Assert
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> learningPathService.startPath(userWithLevel, 2L));

        assertTrue(exception.getMessage().contains("already started"));
        verify(learningPathRepository, times(1)).findById(2L);
        verify(userLearningPathRepository, times(1)).existsByUserIdAndPathId(userWithLevel.getId(), 2L);
        verify(userLearningPathRepository, never()).save(any());
    }

    @Test
    @DisplayName("startPath - Should throw exception when path has no courses")
    void testStartPath_NoCourses() {
        // Arrange
        LearningPath emptyPath = LearningPath.builder()
                .id(3L)
                .name("Empty Path")
                .cefrLevel("A1")
                .learningPathCourses(new ArrayList<>())
                .build();

        when(learningPathRepository.findById(3L)).thenReturn(Optional.of(emptyPath));
        when(userLearningPathRepository.existsByUserIdAndPathId(userWithLevel.getId(), 3L))
                .thenReturn(false);

        // Act & Assert
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> learningPathService.startPath(userWithLevel, 3L));

        assertTrue(exception.getMessage().contains("no courses"));
        verify(userLearningPathRepository, never()).save(any());
    }

    @Test
    @DisplayName("startPath - Should set current course to first course by order index")
    void testStartPath_SetsFirstCourse() {
        // Arrange
        when(learningPathRepository.findById(2L)).thenReturn(Optional.of(pathWithCourses));
        when(userLearningPathRepository.existsByUserIdAndPathId(userWithLevel.getId(), 2L))
                .thenReturn(false);
        when(userLearningPathRepository.save(any(UserLearningPath.class)))
                .thenAnswer(invocation -> {
                    UserLearningPath saved = invocation.getArgument(0);
                    saved.setId(1L);
                    return saved;
                });

        // Act
        UserPathProgressDTO result = learningPathService.startPath(userWithLevel, 2L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getCurrentCourseId()); // Course with orderIndex 0
        assertEquals("English Basics (A1)", result.getCurrentCourseTitle());
    }

    @Test
    @DisplayName("startPath - Should handle path with single course")
    void testStartPath_SingleCourse() {
        // Arrange
        LearningPath singleCoursePath = LearningPath.builder()
                .id(4L)
                .name("Single Course Path")
                .cefrLevel("A1")
                .learningPathCourses(new ArrayList<>())
                .build();

        LearningPathCourse singlePathCourse = LearningPathCourse.builder()
                .learningPath(singleCoursePath)
                .course(course1)
                .orderIndex(0)
                .build();
        singleCoursePath.getLearningPathCourses().add(singlePathCourse);

        when(learningPathRepository.findById(4L)).thenReturn(Optional.of(singleCoursePath));
        when(userLearningPathRepository.existsByUserIdAndPathId(userWithLevel.getId(), 4L))
                .thenReturn(false);
        when(userLearningPathRepository.save(any(UserLearningPath.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        UserPathProgressDTO result = learningPathService.startPath(userWithLevel, 4L);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalCourses());
        assertEquals(1L, result.getCurrentCourseId());
    }

    // ==================== getMyProgress() Tests ====================

    @Test
    @DisplayName("getMyProgress - Should return user's progress for all enrolled paths")
    void testGetMyProgress_Success() {
        // Arrange
        UserLearningPath ulp2 = UserLearningPath.builder()
                .id(2L)
                .user(userWithLevel)
                .learningPath(validPath)
                .currentCourse(course1)
                .startedAt(LocalDateTime.now())
                .build();

        List<UserLearningPath> userPaths = Arrays.asList(userLearningPath, ulp2);
        when(userLearningPathRepository.findByUserId(userWithLevel.getId())).thenReturn(userPaths);

        // Act
        List<UserPathProgressDTO> result = learningPathService.getMyProgress(userWithLevel);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(userLearningPathRepository, times(1)).findByUserId(userWithLevel.getId());
    }

    @Test
    @DisplayName("getMyProgress - Should return empty list when user has no enrollments")
    void testGetMyProgress_NoEnrollments() {
        // Arrange
        when(userLearningPathRepository.findByUserId(userWithLevel.getId()))
                .thenReturn(new ArrayList<>());

        // Act
        List<UserPathProgressDTO> result = learningPathService.getMyProgress(userWithLevel);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(userLearningPathRepository, times(1)).findByUserId(userWithLevel.getId());
    }

    @Test
    @DisplayName("getMyProgress - Should return progress with 0 courses completed (placeholder)")
    void testGetMyProgress_PlaceholderProgress() {
        // Arrange
        List<UserLearningPath> userPaths = Arrays.asList(userLearningPath);
        when(userLearningPathRepository.findByUserId(userWithLevel.getId())).thenReturn(userPaths);

        // Act
        List<UserPathProgressDTO> result = learningPathService.getMyProgress(userWithLevel);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(0, result.get(0).getCoursesCompleted()); // Placeholder value
        assertEquals(0, result.get(0).getProgressPercentage()); // Calculated from 0 completed
        verify(userLearningPathRepository, times(1)).findByUserId(userWithLevel.getId());
    }

    @Test
    @DisplayName("getMyProgress - Should include current course information")
    void testGetMyProgress_IncludesCurrentCourse() {
        // Arrange
        List<UserLearningPath> userPaths = Arrays.asList(userLearningPath);
        when(userLearningPathRepository.findByUserId(userWithLevel.getId())).thenReturn(userPaths);

        // Act
        List<UserPathProgressDTO> result = learningPathService.getMyProgress(userWithLevel);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getCurrentCourseId());
        assertEquals("English Basics (A1)", result.get(0).getCurrentCourseTitle());
    }

    @Test
    @DisplayName("getMyProgress - Should handle completed path")
    void testGetMyProgress_CompletedPath() {
        // Arrange
        UserLearningPath completedPath = UserLearningPath.builder()
                .id(3L)
                .user(userWithLevel)
                .learningPath(pathWithCourses)
                .currentCourse(course2)
                .startedAt(LocalDateTime.now().minusDays(30))
                .completedAt(LocalDateTime.now())
                .build();

        List<UserLearningPath> userPaths = Arrays.asList(completedPath);
        when(userLearningPathRepository.findByUserId(userWithLevel.getId())).thenReturn(userPaths);

        // Act
        List<UserPathProgressDTO> result = learningPathService.getMyProgress(userWithLevel);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.get(0).getIsCompleted());
        assertNotNull(result.get(0).getCompletedAt());
    }

    @Test
    @DisplayName("getMyProgress - Should handle path with null current course")
    void testGetMyProgress_NullCurrentCourse() {
        // Arrange
        UserLearningPath pathWithoutCourse = UserLearningPath.builder()
                .id(4L)
                .user(userWithLevel)
                .learningPath(pathWithCourses)
                .currentCourse(null) // No current course
                .startedAt(LocalDateTime.now())
                .build();

        List<UserLearningPath> userPaths = Arrays.asList(pathWithoutCourse);
        when(userLearningPathRepository.findByUserId(userWithLevel.getId())).thenReturn(userPaths);

        // Act
        List<UserPathProgressDTO> result = learningPathService.getMyProgress(userWithLevel);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertNull(result.get(0).getCurrentCourseId());
        assertNull(result.get(0).getCurrentCourseTitle());
    }
}
