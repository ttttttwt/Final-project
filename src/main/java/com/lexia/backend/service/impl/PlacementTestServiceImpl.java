package com.lexia.backend.service.impl;

import com.lexia.backend.dto.placement.*;
import com.lexia.backend.entity.*;
import com.lexia.backend.repository.*;
import com.lexia.backend.service.PlacementTestService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PlacementTestServiceImpl implements PlacementTestService {

    private final PlacementTestQuestionRepository questionRepository;
    private final UserPlacementResultRepository resultRepository;
    private final UserProfileRepository userProfileRepository;
    private final LearningPathRepository learningPathRepository;
    private final UserLearningPathRepository userLearningPathRepository;

    private static final int QUESTIONS_COUNT = 15;

    @Override
    public List<PlacementQuestionResponseDTO> getPlacementQuestions() {
        List<PlacementTestQuestion> questions = questionRepository.findRandomQuestions(QUESTIONS_COUNT);
        
        return questions.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public PlacementTestResultDTO submitPlacementTest(User user, PlacementTestSubmissionDTO submission) {
        // 1. Calculate Score
        int score = calculateScore(submission.getAnswers());
        
        // 2. Determine Level
        String assignedLevel = determineLevel(score, submission.getAnswers().size());
        
        // 3. Save Result
        UserPlacementResult result = UserPlacementResult.builder()
                .user(user)
                .score(score)
                .totalQuestions(submission.getAnswers().size())
                .assignedLevel(assignedLevel)
                .build();
        resultRepository.save(result);
        
        // 4. Update User Profile
        UserProfile profile = userProfileRepository.findById(user.getId())
                .orElseThrow(() -> new RuntimeException("User profile not found"));
        profile.setCurrentLevel(assignedLevel);
        userProfileRepository.save(profile);
        
        // 5. Assign Learning Path
        LearningPath assignedPath = assignLearningPath(user, assignedLevel);
        
        return PlacementTestResultDTO.builder()
                .score(score)
                .totalQuestions(submission.getAnswers().size())
                .assignedLevel(assignedLevel)
                .message("Congratulations! You have been placed at level " + assignedLevel)
                .assignedLearningPathId(assignedPath != null ? assignedPath.getId() : null)
                .assignedLearningPathName(assignedPath != null ? assignedPath.getName() : "No default path found")
                .build();
    }

    private PlacementQuestionResponseDTO mapToDTO(PlacementTestQuestion question) {
        return PlacementQuestionResponseDTO.builder()
                .id(question.getId())
                .content(question.getContent())
                .optionA(question.getOptionA())
                .optionB(question.getOptionB())
                .optionC(question.getOptionC())
                .optionD(question.getOptionD())
                .category(question.getCategory())
                .build();
    }

    private int calculateScore(List<PlacementAnswerDTO> answers) {
        if (answers == null || answers.isEmpty()) return 0;
        
        List<UUID> questionIds = answers.stream()
                .map(PlacementAnswerDTO::getQuestionId)
                .collect(Collectors.toList());
        
        Map<UUID, PlacementTestQuestion> questionMap = questionRepository.findAllById(questionIds).stream()
                .collect(Collectors.toMap(PlacementTestQuestion::getId, Function.identity()));
        
        int score = 0;
        for (PlacementAnswerDTO answer : answers) {
            PlacementTestQuestion question = questionMap.get(answer.getQuestionId());
            if (question != null && question.getCorrectOption().equalsIgnoreCase(answer.getSelectedOption())) {
                score++;
            }
        }
        return score;
    }

    private String determineLevel(int score, int total) {
        double percentage = (double) score / total * 100;
        
        if (percentage < 20) return "A1";
        if (percentage < 40) return "A2";
        if (percentage < 60) return "B1";
        if (percentage < 80) return "B2";
        return "C1"; // C1/C2
    }

    private LearningPath assignLearningPath(User user, String level) {
        Optional<LearningPath> pathOpt = learningPathRepository.findByCefrLevelAndIsDefaultTrue(level);
        
        if (pathOpt.isPresent()) {
            LearningPath path = pathOpt.get();
            
            // Check if already enrolled
            boolean alreadyEnrolled = userLearningPathRepository.existsByUserIdAndPathId(user.getId(), path.getId());
            
            if (!alreadyEnrolled) {
                UserLearningPath enrollment = UserLearningPath.builder()
                        .user(user)
                        .learningPath(path)
                        .build();
                userLearningPathRepository.save(enrollment);
            }
            return path;
        }
        return null;
    }
}
