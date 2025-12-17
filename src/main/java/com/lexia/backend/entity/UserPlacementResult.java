package com.lexia.backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "user_placement_results")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserPlacementResult {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "UUID")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NotNull
    @Column(nullable = false)
    private Integer score;

    @NotNull
    @Column(name = "total_questions", nullable = false)
    private Integer totalQuestions;

    @NotBlank
    @Column(name = "assigned_level", nullable = false, length = 10)
    private String assignedLevel; // A1, A2, etc.

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
