package com.lexia.backend.dto.ai;

import lombok.Data;

@Data
public class AIFeatureConfig {
    private String featureId;
    private String name;
    private boolean enabled;
    private String modelId;
    private int maxTokens;
    private double temperature;
    private int dailyLimit;
}
