package com.lexia.backend.dto.admin;

import lombok.Data;

@Data
public class AdminRefundRequestDTO {
    private String reason; // requested_by_customer, duplicate, fraudulent
}
