package com.lexia.backend.service.ai;

import com.lexia.backend.entity.UserAiQuota;
import com.lexia.backend.repository.UserAiQuotaRepository;
import com.lexia.backend.service.ai.impl.AIQuotaServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AIQuotaServiceTest {

    @Mock
    private UserAiQuotaRepository quotaRepository;

    @InjectMocks
    private AIQuotaServiceImpl quotaService;

    @Test
    void getAllQuotas_ShouldReturnPage() {
        PageRequest pageable = PageRequest.of(0, 10);
        when(quotaRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(new UserAiQuota())));
        
        Page<UserAiQuota> result = quotaService.getAllQuotas(pageable);
        
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void getQuotaByUserId_ShouldReturnQuota() {
        UUID userId = UUID.randomUUID();
        UserAiQuota quota = new UserAiQuota();
        quota.setUserId(userId);
        when(quotaRepository.findById(userId)).thenReturn(Optional.of(quota));
        
        UserAiQuota result = quotaService.getQuotaByUserId(userId);
        
        assertEquals(userId, result.getUserId());
    }

    @Test
    void resetQuota_ShouldResetUsage() {
        UUID userId = UUID.randomUUID();
        UserAiQuota quota = new UserAiQuota();
        quota.setDailyUsed(10);
        quota.setMonthlyUsed(100);
        when(quotaRepository.findById(userId)).thenReturn(Optional.of(quota));
        
        quotaService.resetQuota(userId);
        
        assertEquals(0, quota.getDailyUsed());
        assertEquals(0, quota.getMonthlyUsed());
        verify(quotaRepository).save(quota);
    }
}
