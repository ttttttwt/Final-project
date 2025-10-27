package com.lexia.backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "user_profiles")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfile {

    @Id
    @Column(name = "user_id")
    private UUID userId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private User user;

    @Size(max = 255)
    @Column(name = "full_name", length = 255)
    private String fullName;

    @Size(max = 100)
    @Column(name = "first_name", length = 100)
    private String firstName;

    @Size(max = 100)
    @Column(name = "last_name", length = 100)
    private String lastName;

    @Size(max = 500)
    @Column(name = "bio", length = 500)
    private String bio;

    @Pattern(regexp = "^[+]?[0-9]{10,20}$", message = "Phone number must be 10-20 digits, optionally starting with +")
    @Size(max = 20)
    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @Size(max = 255)
    @Column(name = "avatar_url", length = 255)
    private String avatarUrl;

    @Size(max = 50)
    @Column(name = "timezone", length = 50)
    @Builder.Default
    private String timezone = "UTC";

    @Pattern(regexp = "^[a-z]{2}$", message = "Language must be a valid ISO 639-1 code (e.g., en, vi)")
    @Size(max = 10)
    @Column(name = "language", length = 10)
    @Builder.Default
    private String language = "en";

    @Size(max = 10)
    @Column(name = "current_level", length = 10)
    private String currentLevel;

    @Column(name = "learning_goal", columnDefinition = "TEXT")
    private String learningGoal;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}