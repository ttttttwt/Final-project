package com.lexia.backend.dto.ai;

import lombok.Data;
import java.util.List;

@Data
public class AIGlobalSettings {
    private int defaultDailyLimit;
    private int defaultMonthlyLimit;
    private double costPerToken;
    private List<AIFeatureConfig> features;
}
