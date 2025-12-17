package com.lexia.backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "placement_test_questions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlacementTestQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "UUID")
    private UUID id;

    @NotBlank
    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @NotBlank
    @Column(name = "option_a", nullable = false)
    private String optionA;

    @NotBlank
    @Column(name = "option_b", nullable = false)
    private String optionB;

    @NotBlank
    @Column(name = "option_c", nullable = false)
    private String optionC;

    @NotBlank
    @Column(name = "option_d", nullable = false)
    private String optionD;

    @NotBlank
    @Column(name = "correct_option", nullable = false, length = 1)
    private String correctOption; // A, B, C, or D

    @NotBlank
    @Column(name = "difficulty_level", nullable = false, length = 2)
    private String difficultyLevel; // A1, A2, B1, B2, C1, C2

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private QuestionCategory category;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public enum QuestionCategory {
        GRAMMAR,
        VOCABULARY,
        SITUATIONAL
    }
}
