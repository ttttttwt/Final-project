package com.lexia.backend.dto;

import lombok.Data;

@Data
public class CheckoutRequest {
    private String planType; // MONTHLY or YEARLY
}
