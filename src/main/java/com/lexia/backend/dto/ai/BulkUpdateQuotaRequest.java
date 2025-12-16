package com.lexia.backend.dto.ai;

import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class BulkUpdateQuotaRequest {
    private List<UUID> userIds;
    private UpdateQuotaRequest quotaDetails;
}
