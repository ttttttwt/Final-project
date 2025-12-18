package com.lexia.backend.service.impl;

import com.lexia.backend.dto.AdminUserDTO;
import com.lexia.backend.dto.AdminUserDetailDTO;
import com.lexia.backend.dto.CreateUserDTO;
import com.lexia.backend.dto.UpdateUserDTO;
import com.lexia.backend.entity.*;
import com.lexia.backend.exception.ResourceNotFoundException;
import com.lexia.backend.exception.UserAlreadyExistsException;
import com.lexia.backend.repository.*;
import com.lexia.backend.service.AdminUserService;
import com.lexia.backend.specification.UserSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminUserServiceImpl implements AdminUserService {

        private final UserRepository userRepository;
        private final UserProfileRepository userProfileRepository;
        private final RoleRepository roleRepository;
        private final UserRoleRepository userRoleRepository;
        private final PasswordEncoder passwordEncoder;
        private final EnrollmentRepository enrollmentRepository;
        private final UserLearningPathRepository userLearningPathRepository;
        private final UserPlacementResultRepository userPlacementResultRepository;
        private final SubscriptionRepository subscriptionRepository;
        private final UserAiQuotaRepository userAiQuotaRepository;
        private final AIUsageLogRepository aiUsageLogRepository;

        @Override
        @Transactional(readOnly = true)
        public Page<AdminUserDTO> getAllUsers(String search, String role, Pageable pageable) {
                Specification<User> spec = UserSpecification.withSearchAndRole(search, role);
                Page<User> usersPage = userRepository.findAll(spec, pageable);

                return usersPage.map(this::mapToAdminUserDTO);
        }

        @Override
        @Transactional(readOnly = true)
        public AdminUserDTO getUserById(UUID id) {
                User user = userRepository.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
                return mapToAdminUserDTO(user);
        }

        @Override
        @Transactional(readOnly = true)
        public AdminUserDetailDTO getUserDetailById(UUID id) {
                User user = userRepository.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

                UserProfile profile = user.getProfile();
                List<String> roles = user.getUserRoles().stream()
                                .map(ur -> ur.getRole().getName())
                                .collect(Collectors.toList());

                // ==================== Placement History ====================
                List<UserPlacementResult> placementResults = userPlacementResultRepository
                                .findByUserIdOrderByCreatedAtDesc(user.getId());

                List<AdminUserDetailDTO.PlacementResultDTO> placementHistory = placementResults.stream()
                                .map(pr -> AdminUserDetailDTO.PlacementResultDTO.builder()
                                                .id(pr.getId())
                                                .score(pr.getScore())
                                                .totalQuestions(pr.getTotalQuestions())
                                                .assignedLevel(pr.getAssignedLevel())
                                                .createdAt(pr.getCreatedAt())
                                                .build())
                                .collect(Collectors.toList());

                String currentCefrLevel = placementResults.isEmpty() ? null
                                : placementResults.get(0).getAssignedLevel();

                // ==================== Subscription ====================
                AdminUserDetailDTO.SubscriptionInfoDTO subscriptionInfo = subscriptionRepository
                                .findByUserId(user.getId())
                                .map(sub -> AdminUserDetailDTO.SubscriptionInfoDTO.builder()
                                                .planType(sub.getPlanType() != null ? sub.getPlanType().name() : "FREE")
                                                .status(sub.getStatus() != null ? sub.getStatus().name() : null)
                                                .startDate(sub.getCreatedAt())
                                                .endDate(sub.getCurrentPeriodEnd())
                                                .stripeCustomerId(sub.getStripeCustomerId())
                                                .build())
                                .orElse(AdminUserDetailDTO.SubscriptionInfoDTO.builder()
                                                .planType("FREE")
                                                .status("ACTIVE")
                                                .build());

                // ==================== Enrolled Courses ====================
                List<Enrollment> enrollments = enrollmentRepository.findByUserId(user.getId());
                List<AdminUserDetailDTO.EnrollmentSummaryDTO> enrolledCourses = enrollments.stream()
                                .map(e -> AdminUserDetailDTO.EnrollmentSummaryDTO.builder()
                                                .courseId(e.getCourse() != null ? e.getCourse().getId() : null)
                                                .courseName(e.getCourse() != null ? e.getCourse().getTitle()
                                                                : "Unknown")
                                                .courseThumbnail(e.getCourse() != null
                                                                ? e.getCourse().getEffectiveThumbnailUrl()
                                                                : null)
                                                .cefrLevel(e.getCourse() != null ? e.getCourse().getCefrLevel() : null)
                                                .progressPercentage(e.getProgressPercentage())
                                                .isCompleted(e.isCompleted())
                                                .enrolledAt(e.getEnrolledAt())
                                                .completedAt(e.getCompletedAt())
                                                .build())
                                .collect(Collectors.toList());

                // ==================== Enrolled Learning Paths ====================
                List<UserLearningPath> learningPaths = userLearningPathRepository.findByUserId(user.getId());
                List<AdminUserDetailDTO.LearningPathSummaryDTO> enrolledPaths = learningPaths.stream()
                                .map(ulp -> AdminUserDetailDTO.LearningPathSummaryDTO.builder()
                                                .pathId(ulp.getLearningPath() != null ? ulp.getLearningPath().getId()
                                                                : null)
                                                .pathName(ulp.getLearningPath() != null
                                                                ? ulp.getLearningPath().getName()
                                                                : "Unknown")
                                                .pathDescription(ulp.getLearningPath() != null
                                                                ? ulp.getLearningPath().getDescription()
                                                                : null)
                                                .currentCourseIndex(
                                                                ulp.getCurrentCourse() != null
                                                                                ? ulp.getCurrentCourse().getId()
                                                                                                .intValue()
                                                                                : 0)
                                                .totalCourses(0) // LearningPath entity doesn't have totalCourses
                                                .overallProgress(BigDecimal.ZERO) // Placeholder
                                                .startedAt(ulp.getStartedAt())
                                                .completedAt(ulp.getCompletedAt())
                                                .build())
                                .collect(Collectors.toList());

                // ==================== Learning Stats ====================
                int completedCourses = (int) enrollments.stream()
                                .filter(Enrollment::isCompleted)
                                .count();
                int completedPaths = (int) learningPaths.stream()
                                .filter(p -> p.getCompletedAt() != null)
                                .count();

                AdminUserDetailDTO.LearningStatsDTO learningStats = AdminUserDetailDTO.LearningStatsDTO.builder()
                                .totalEnrolledCourses(enrollments.size())
                                .completedCourses(completedCourses)
                                .totalEnrolledPaths(learningPaths.size())
                                .completedPaths(completedPaths)
                                .completedLessons(0) // Placeholder - no lesson tracking in Enrollment
                                .currentStreak(0) // Placeholder - no streak system yet
                                .bestStreak(0) // Placeholder
                                .totalStudyTimeMinutes(0L) // Placeholder
                                .rolePlaySessions(0) // Will be populated from AI logs
                                .flashcardDecksCreated(0)
                                .grammarExercisesCompleted(0)
                                .build();

                // ==================== AI Quota ====================
                AdminUserDetailDTO.AiQuotaSummaryDTO aiQuota = userAiQuotaRepository
                                .findByUserId(user.getId())
                                .map(q -> {
                                        Map<String, AdminUserDetailDTO.FeatureQuotaDTO> featureQuotas = new HashMap<>();
                                        // Add feature quotas for roleplay, grammar, flashcard
                                        for (String feature : List.of("roleplay", "grammar", "flashcard")) {
                                                featureQuotas.put(feature, AdminUserDetailDTO.FeatureQuotaDTO.builder()
                                                                .dailyLimit(q.getFeatureDailyLimit(feature))
                                                                .dailyUsed(q.getFeatureDailyUsage(feature))
                                                                .monthlyLimit(q.getFeatureMonthlyLimit(feature))
                                                                .monthlyUsed(q.getFeatureMonthlyUsage(feature))
                                                                .build());
                                        }
                                        return AdminUserDetailDTO.AiQuotaSummaryDTO.builder()
                                                        .isPremium(q.getIsPremium())
                                                        .dailyLimit(q.getDailyLimit())
                                                        .dailyUsed(q.getDailyUsed())
                                                        .monthlyLimit(q.getMonthlyLimit())
                                                        .monthlyUsed(q.getMonthlyUsed())
                                                        .featureQuotas(featureQuotas)
                                                        .lastResetAt(q.getLastResetDaily() != null
                                                                        ? LocalDateTime.ofInstant(q.getLastResetDaily(),
                                                                                        java.time.ZoneId.systemDefault())
                                                                        : null)
                                                        .isSuspended(q.getSuspended())
                                                        .build();
                                })
                                .orElse(null);

                // ==================== AI Usage Logs ====================
                List<AIUsageLog> aiLogs = aiUsageLogRepository.findTop20ByUserIdOrderByCreatedAtDesc(user.getId());
                List<AdminUserDetailDTO.AiUsageDTO> recentAiUsage = aiLogs.stream()
                                .map(log -> AdminUserDetailDTO.AiUsageDTO.builder()
                                                .id(log.getId())
                                                .contentType(log.getContentType())
                                                .modelId(log.getModelId())
                                                .inputTokens(log.getInputTokens())
                                                .outputTokens(log.getOutputTokens())
                                                .estimatedCostUsd(log.getEstimatedCostUsd())
                                                .responseTimeMs(log.getResponseTimeMs())
                                                .success(log.getSuccess())
                                                .createdAt(log.getCreatedAt())
                                                .build())
                                .collect(Collectors.toList());

                // ==================== Build Final DTO ====================
                return AdminUserDetailDTO.builder()
                                // Basic info
                                .id(user.getId())
                                .email(user.getEmail())
                                .firstName(profile != null ? profile.getFirstName() : null)
                                .lastName(profile != null ? profile.getLastName() : null)
                                .avatarUrl(profile != null ? profile.getAvatarUrl() : null)
                                .phoneNumber(profile != null ? profile.getPhoneNumber() : null)
                                .bio(profile != null ? profile.getBio() : null)
                                .timezone(profile != null ? profile.getTimezone() : null)
                                .language(profile != null ? profile.getLanguage() : null)
                                .roles(roles)
                                .isActive(user.getIsActive())
                                .isDeleted(user.getIsDeleted())
                                .deletedAt(user.getDeletedAt())
                                .createdAt(user.getCreatedAt())
                                .lastActiveAt(profile != null ? profile.getUpdatedAt() : user.getUpdatedAt())
                                .authProvider(user.getAuthProvider() != null ? user.getAuthProvider().name() : null)
                                // CEFR & Placement
                                .currentCefrLevel(currentCefrLevel)
                                .learningGoal(profile != null ? profile.getLearningGoal() : null)
                                .placementHistory(placementHistory)
                                // Subscription
                                .subscription(subscriptionInfo)
                                // Learning Progress
                                .enrolledCourses(enrolledCourses)
                                .enrolledPaths(enrolledPaths)
                                .learningStats(learningStats)
                                // AI Usage
                                .aiQuota(aiQuota)
                                .recentAiUsage(recentAiUsage)
                                // Activity (placeholder - would need activity log repository)
                                .recentActivities(Collections.emptyList())
                                .build();
        }

        @Override
        @Transactional
        public AdminUserDTO createUser(CreateUserDTO createUserDTO) {
                if (userRepository.existsByEmail(createUserDTO.getEmail())) {
                        throw new UserAlreadyExistsException("Email already registered: " + createUserDTO.getEmail());
                }

                // 1. Create User
                User user = User.builder()
                                .email(createUserDTO.getEmail())
                                .passwordHash(passwordEncoder.encode(createUserDTO.getPassword()))
                                .authProvider(User.AuthProvider.EMAIL)
                                .isActive(true)
                                .build();

                user = userRepository.save(user);

                // 2. Create Profile
                UserProfile profile = UserProfile.builder()
                                .user(user)
                                .firstName(createUserDTO.getFirstName())
                                .lastName(createUserDTO.getLastName())
                                .fullName(createUserDTO.getFirstName() + " " + createUserDTO.getLastName())
                                .build();

                userProfileRepository.save(profile);
                user.setProfile(profile);

                // 3. Assign Role
                Role role = roleRepository.findByName(createUserDTO.getRole())
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Role not found: " + createUserDTO.getRole()));

                UserRole userRole = new UserRole(user, role);
                userRoleRepository.save(userRole);

                // Refresh to get relationships
                return mapToAdminUserDTO(user);
        }

        @Override
        @Transactional
        public AdminUserDTO updateUser(UUID id, UpdateUserDTO updateUserDTO) {
                User user = userRepository.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

                // Update User fields
                if (updateUserDTO.getIsActive() != null) {
                        user.setIsActive(updateUserDTO.getIsActive());
                }
                if (updateUserDTO.getPassword() != null && !updateUserDTO.getPassword().isEmpty()) {
                        user.setPasswordHash(passwordEncoder.encode(updateUserDTO.getPassword()));
                }
                userRepository.save(user);

                // Update Profile fields
                UserProfile profile = user.getProfile();
                if (profile == null) {
                        profile = UserProfile.builder().user(user).build();
                }

                boolean nameChanged = false;
                if (updateUserDTO.getFirstName() != null) {
                        profile.setFirstName(updateUserDTO.getFirstName());
                        nameChanged = true;
                }
                if (updateUserDTO.getLastName() != null) {
                        profile.setLastName(updateUserDTO.getLastName());
                        nameChanged = true;
                }
                if (nameChanged) {
                        profile.setFullName(profile.getFirstName() + " " + profile.getLastName());
                }
                userProfileRepository.save(profile);

                // Update Role if provided
                if (updateUserDTO.getRole() != null) {
                        // Remove existing roles
                        userRoleRepository.deleteAll(user.getUserRoles());
                        user.getUserRoles().clear();

                        // Add new role
                        Role role = roleRepository.findByName(updateUserDTO.getRole())
                                        .orElseThrow(() -> new ResourceNotFoundException(
                                                        "Role not found: " + updateUserDTO.getRole()));

                        UserRole userRole = new UserRole(user, role);
                        userRoleRepository.save(userRole);
                }

                return mapToAdminUserDTO(user);
        }

        @Override
        @Transactional
        public void deleteUser(UUID id) {
                User user = userRepository.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

                // Soft delete: mark as deleted but keep data
                user.setIsDeleted(true);
                user.setDeletedAt(LocalDateTime.now());
                userRepository.save(user);

                log.info("User soft deleted: {} (email: {})", id, user.getEmail());
        }

        @Override
        @Transactional
        public void hardDeleteUser(UUID id, String reason) {
                User user = userRepository.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

                String email = user.getEmail();

                // Actually delete from database
                userRepository.delete(user);

                log.warn("User HARD DELETED: {} (email: {}). Reason: {}", id, email, reason);
        }

        @Override
        @Transactional
        public AdminUserDTO restoreUser(UUID id) {
                User user = userRepository.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

                if (!Boolean.TRUE.equals(user.getIsDeleted())) {
                        throw new IllegalStateException("User is not deleted: " + id);
                }

                // Restore: clear deleted flags
                user.setIsDeleted(false);
                user.setDeletedAt(null);
                userRepository.save(user);

                log.info("User restored: {} (email: {})", id, user.getEmail());
                return mapToAdminUserDTO(user);
        }

        @Override
        @Transactional(readOnly = true)
        public Page<AdminUserDTO> getDeletedUsers(Pageable pageable) {
                Page<User> deletedUsersPage = userRepository.findByIsDeletedTrue(pageable);
                return deletedUsersPage.map(this::mapToAdminUserDTO);
        }

        private AdminUserDTO mapToAdminUserDTO(User user) {
                List<String> roles = user.getUserRoles().stream()
                                .map(ur -> ur.getRole().getName())
                                .collect(Collectors.toList());

                String firstName = user.getProfile() != null ? user.getProfile().getFirstName() : null;
                String lastName = user.getProfile() != null ? user.getProfile().getLastName() : null;

                // Get CEFR level from latest placement result
                String cefrLevel = userPlacementResultRepository
                                .findTopByUserIdOrderByCreatedAtDesc(user.getId())
                                .map(result -> result.getAssignedLevel())
                                .orElse(null);

                // Count enrollments
                int enrolledCoursesCount = enrollmentRepository.countByUserId(user.getId());
                int enrolledPathsCount = userLearningPathRepository.countByUserId(user.getId());

                // Last active (using profile's updatedAt as proxy)
                LocalDateTime lastActiveAt = user.getProfile() != null
                                ? user.getProfile().getUpdatedAt()
                                : user.getUpdatedAt();

                return AdminUserDTO.builder()
                                .id(user.getId())
                                .email(user.getEmail())
                                .firstName(firstName)
                                .lastName(lastName)
                                .roles(roles)
                                .isActive(user.getIsActive())
                                .createdAt(user.getCreatedAt())
                                // Soft delete fields
                                .isDeleted(user.getIsDeleted())
                                .deletedAt(user.getDeletedAt())
                                // Enhanced user info
                                .cefrLevel(cefrLevel)
                                .enrolledCoursesCount(enrolledCoursesCount)
                                .enrolledPathsCount(enrolledPathsCount)
                                .subscriptionType("FREE") // Placeholder - no subscription system yet
                                .lastActiveAt(lastActiveAt)
                                .streakDays(0) // Placeholder - no streak system yet
                                .build();
        }
}
