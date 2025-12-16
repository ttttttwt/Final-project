package com.lexia.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@Entity
@Table(name = "ai_configs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AIConfig {

    @Id
    @Column(name = "config_key", length = 100)
    private String configKey;

    @Column(name = "config_value", columnDefinition = "TEXT")
    private String configValue;

    @Column(name = "description")
    private String description;

    @Column(name = "is_encrypted")
    @Builder.Default
    private Boolean isEncrypted = false;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;
}
