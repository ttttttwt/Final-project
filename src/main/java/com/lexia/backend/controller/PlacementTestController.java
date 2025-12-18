package com.lexia.backend.controller;

import com.lexia.backend.dto.placement.PlacementQuestionResponseDTO;
import com.lexia.backend.dto.placement.PlacementTestResultDTO;
import com.lexia.backend.dto.placement.PlacementTestSubmissionDTO;
import com.lexia.backend.entity.User;
import com.lexia.backend.repository.UserRepository;
import com.lexia.backend.service.PlacementTestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/placement-test")
@RequiredArgsConstructor
@Tag(name = "Placement Test API", description = "Endpoints for taking the placement test and getting results")
@SecurityRequirement(name = "bearerAuth")
public class PlacementTestController {

    private final PlacementTestService placementTestService;
    private final UserRepository userRepository;

    @GetMapping("/questions")
    @Operation(summary = "Get random placement test questions", description = "Returns a list of 15 random questions for the placement test.")
    public ResponseEntity<List<PlacementQuestionResponseDTO>> getQuestions() {
        return ResponseEntity.ok(placementTestService.getPlacementQuestions());
    }

    @PostMapping("/submit")
    @Operation(summary = "Submit placement test answers", description = "Calculates score, assigns level, and creates a learning path.")
    public ResponseEntity<PlacementTestResultDTO> submitTest(@Valid @RequestBody PlacementTestSubmissionDTO submission) {
        User user = getCurrentUser();
        return ResponseEntity.ok(placementTestService.submitPlacementTest(user, submission));
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication != null && authentication.getPrincipal() instanceof User) {
            return (User) authentication.getPrincipal();
        }

        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}
