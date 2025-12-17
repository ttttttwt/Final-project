package com.lexia.backend.dto.ai;

import lombok.Data;
import java.util.List;

@Data
public class AIGlobalSettings {
    private boolean globalEnabled;
    private double monthlyBudgetLimit;
    private int alertThresholdPercentage;
    private boolean fallbackEnabled;
    private boolean rateLimitEnabled;
    private double costPerInputToken;
    private double costPerOutputToken;
    
    // Legacy/Compatibility fields (can be mapped to new ones or kept)
    private int defaultDailyLimit;
    private int defaultMonthlyLimit;
    private double costPerToken;
    
    private List<AIFeatureConfig> features;
}
