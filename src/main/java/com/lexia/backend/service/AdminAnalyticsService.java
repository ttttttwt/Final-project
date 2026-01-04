package com.lexia.backend.service;

import com.lexia.backend.dto.admin.AnalyticsDTO.*;
import com.lexia.backend.enums.PlanType;
import com.lexia.backend.enums.SubscriptionStatus;
import com.lexia.backend.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Service for analytics dashboard data aggregation
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class AdminAnalyticsService {

    private final UserRepository userRepository;
    private final AIUsageLogRepository aiUsageLogRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final PaymentRepository paymentRepository;

    /**
     * Get complete analytics data for dashboard
     */
    public AnalyticsResponse getAnalytics() {
        log.info("Fetching analytics data");

        return AnalyticsResponse.builder()
                .overview(getOverviewStats())
                .monthlyStats(getMonthlyStats(6))
                .userDistribution(getUserDistribution())
                .aiUsage(getAIUsageStats())
                .build();
    }

    /**
     * Get overview statistics
     */
    public OverviewStats getOverviewStats() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfMonth = now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        LocalDateTime thirtyDaysAgo = now.minusDays(30);

        long totalUsers = userRepository.count();
        long activeUsers = userRepository.countActiveUsersAfter(thirtyDaysAgo);
        long newUsersThisMonth = userRepository.countUsersCreatedAfter(startOfMonth);

        // Count by subscription type
        long monthlyPro = subscriptionRepository.countByStatusAndPlanType(SubscriptionStatus.ACTIVE, PlanType.MONTHLY);
        long yearlyPro = subscriptionRepository.countByStatusAndPlanType(SubscriptionStatus.ACTIVE, PlanType.YEARLY);
        long proUsers = monthlyPro + yearlyPro;
        long freeUsers = totalUsers - proUsers;

        // Revenue stats from payments table
        BigDecimal totalRevenue = paymentRepository.sumTotalRevenue();
        BigDecimal revenueThisMonth = paymentRepository.sumRevenueAfter(startOfMonth);

        // AI stats
        long totalAIRequests = aiUsageLogRepository.count();
        long aiRequestsThisMonth = aiUsageLogRepository.countByCreatedAtAfter(startOfMonth.atZone(ZoneId.systemDefault()).toInstant());

        return OverviewStats.builder()
                .totalUsers(totalUsers)
                .activeUsers(activeUsers)
                .newUsersThisMonth(newUsersThisMonth)
                .freeUsers(freeUsers)
                .proUsers(proUsers)
                .totalRevenue(totalRevenue != null ? totalRevenue : BigDecimal.ZERO)
                .revenueThisMonth(revenueThisMonth != null ? revenueThisMonth : BigDecimal.ZERO)
                .totalAIRequests(totalAIRequests)
                .aiRequestsThisMonth(aiRequestsThisMonth)
                .build();
    }

    /**
     * Get monthly statistics for the last N months
     */
    public List<MonthlyStats> getMonthlyStats(int months) {
        List<MonthlyStats> stats = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");

        for (int i = months - 1; i >= 0; i--) {
            YearMonth yearMonth = YearMonth.now().minusMonths(i);
            LocalDateTime startOfMonth = yearMonth.atDay(1).atStartOfDay();
            LocalDateTime endOfMonth = yearMonth.atEndOfMonth().atTime(23, 59, 59);

            long newUsers = userRepository.countUsersCreatedBetween(startOfMonth, endOfMonth);
            long activeUsers = userRepository.countActiveUsersBetween(startOfMonth, endOfMonth);
            BigDecimal revenue = paymentRepository.sumRevenueBetween(startOfMonth, endOfMonth);
            long aiRequests = aiUsageLogRepository.countByCreatedAtBetween(
                    startOfMonth.atZone(ZoneId.systemDefault()).toInstant(),
                    endOfMonth.atZone(ZoneId.systemDefault()).toInstant());

            stats.add(MonthlyStats.builder()
                    .month(yearMonth.format(formatter))
                    .newUsers(newUsers)
                    .activeUsers(activeUsers)
                    .revenue(revenue != null ? revenue : BigDecimal.ZERO)
                    .aiRequests(aiRequests)
                    .build());
        }

        return stats;
    }

    /**
     * Get user distribution statistics
     */
    public UserDistribution getUserDistribution() {
        long totalUsers = userRepository.count();
        long monthlyPro = subscriptionRepository.countByStatusAndPlanType(SubscriptionStatus.ACTIVE, PlanType.MONTHLY);
        long yearlyPro = subscriptionRepository.countByStatusAndPlanType(SubscriptionStatus.ACTIVE, PlanType.YEARLY);
        long freeUsers = totalUsers - (monthlyPro + yearlyPro);

        // Get users by level
        Map<String, Long> usersByLevel = new HashMap<>();
        usersByLevel.put("BEGINNER", userRepository.countByLevel("BEGINNER"));
        usersByLevel.put("ELEMENTARY", userRepository.countByLevel("ELEMENTARY"));
        usersByLevel.put("INTERMEDIATE", userRepository.countByLevel("INTERMEDIATE"));
        usersByLevel.put("UPPER_INTERMEDIATE", userRepository.countByLevel("UPPER_INTERMEDIATE"));
        usersByLevel.put("ADVANCED", userRepository.countByLevel("ADVANCED"));

        return UserDistribution.builder()
                .freeUsers(freeUsers)
                .monthlyProUsers(monthlyPro)
                .yearlyProUsers(yearlyPro)
                .usersByLevel(usersByLevel)
                .build();
    }

    /**
     * Get AI usage statistics
     */
    public AIUsageStats getAIUsageStats() {
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        java.time.Instant thirtyDaysAgoInstant = thirtyDaysAgo.atZone(ZoneId.systemDefault()).toInstant();

        long totalRequests = aiUsageLogRepository.countByCreatedAtAfter(thirtyDaysAgoInstant);
        long roleplayRequests = aiUsageLogRepository.countByFeatureAndCreatedAtAfter("ROLEPLAY", thirtyDaysAgoInstant);
        long grammarRequests = aiUsageLogRepository.countByFeatureAndCreatedAtAfter("GRAMMAR", thirtyDaysAgoInstant);
        long flashcardRequests = aiUsageLogRepository.countByFeatureAndCreatedAtAfter("FLASHCARD", thirtyDaysAgoInstant);
        long translationRequests = aiUsageLogRepository.countByFeatureAndCreatedAtAfter("TRANSLATION", thirtyDaysAgoInstant);

        // Calculate success rate
        long successfulRequests = aiUsageLogRepository.countBySuccessAndCreatedAtAfter(true, thirtyDaysAgoInstant);
        long successRate = totalRequests > 0 ? (successfulRequests * 100) / totalRequests : 0;

        // Average response time
        Double avgResponseTime = aiUsageLogRepository.averageResponseTimeAfter(thirtyDaysAgoInstant);

        // Daily usage for chart (last 14 days)
        List<DailyAIUsage> dailyUsage = getDailyAIUsage(14);

        return AIUsageStats.builder()
                .totalRequests(totalRequests)
                .roleplayRequests(roleplayRequests)
                .grammarRequests(grammarRequests)
                .flashcardRequests(flashcardRequests)
                .translationRequests(translationRequests)
                .successRate(successRate)
                .averageResponseTimeMs(avgResponseTime != null ? avgResponseTime.longValue() : 0)
                .dailyUsage(dailyUsage)
                .build();
    }

    /**
     * Get daily AI usage for the last N days
     */
    private List<DailyAIUsage> getDailyAIUsage(int days) {
        List<DailyAIUsage> usage = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        for (int i = days - 1; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            LocalDateTime startOfDay = date.atStartOfDay();
            LocalDateTime endOfDay = date.atTime(23, 59, 59);

            long requests = aiUsageLogRepository.countByCreatedAtBetween(
                    startOfDay.atZone(ZoneId.systemDefault()).toInstant(),
                    endOfDay.atZone(ZoneId.systemDefault()).toInstant());
            Long tokens = aiUsageLogRepository.sumTokensUsedBetween(
                    startOfDay.atZone(ZoneId.systemDefault()).toInstant(),
                    endOfDay.atZone(ZoneId.systemDefault()).toInstant());

            usage.add(DailyAIUsage.builder()
                    .date(date.format(formatter))
                    .requests(requests)
                    .tokensUsed(tokens != null ? tokens : 0)
                    .build());
        }

        return usage;
    }
}
