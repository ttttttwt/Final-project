package com.lexia.backend.controller;

import com.lexia.backend.dto.CheckoutRequest;
import com.lexia.backend.dto.SubscriptionDTO;
import com.lexia.backend.entity.Subscription;
import com.lexia.backend.entity.User;
import com.lexia.backend.service.StripeService;
import com.lexia.backend.service.SubscriptionService;
import com.lexia.backend.repository.UserRepository;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/subscription")
@RequiredArgsConstructor
public class SubscriptionController {

    private final StripeService stripeService;
    private final SubscriptionService subscriptionService;
    private final UserRepository userRepository;

    @Value("${stripe.price-id.monthly}")
    private String monthlyPriceId;

    @Value("${stripe.price-id.yearly}")
    private String yearlyPriceId;
    
    @Value("${app.frontend-url:http://localhost:3000}")
    private String frontendUrl;

    @GetMapping("/status")
    public ResponseEntity<SubscriptionDTO> getSubscriptionStatus(@AuthenticationPrincipal User user) {
        Subscription subscription = subscriptionService.getSubscription(user.getId());
        
        SubscriptionDTO dto = SubscriptionDTO.builder()
                .id(subscription.getId())
                .planType(subscription.getPlanType())
                .status(subscription.getStatus())
                .currentPeriodEnd(subscription.getCurrentPeriodEnd())
                .stripeCustomerId(subscription.getStripeCustomerId())
                .build();
                
        return ResponseEntity.ok(dto);
    }

    @PostMapping("/checkout")
    public ResponseEntity<Map<String, String>> createCheckoutSession(
            @AuthenticationPrincipal User user,
            @RequestBody CheckoutRequest request) throws StripeException {
        
        Subscription subscription = subscriptionService.getSubscription(user.getId());
        
        String planType = "YEARLY".equalsIgnoreCase(request.getPlanType()) ? "YEARLY" : "MONTHLY";
        String priceId = "YEARLY".equalsIgnoreCase(request.getPlanType()) ? yearlyPriceId : monthlyPriceId;
        String successUrl = frontendUrl + "/pricing/success?session_id={CHECKOUT_SESSION_ID}";
        String cancelUrl = frontendUrl + "/pricing/cancel";

        Session session = stripeService.createCheckoutSession(subscription.getStripeCustomerId(), priceId, successUrl, cancelUrl, user.getId().toString(), planType);
        
        Map<String, String> response = new HashMap<>();
        response.put("url", session.getUrl());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/portal")
    public ResponseEntity<Map<String, String>> createPortalSession(@AuthenticationPrincipal User user) throws StripeException {
        Subscription subscription = subscriptionService.getSubscription(user.getId());
        
        if (subscription.getStripeCustomerId() == null) {
             throw new RuntimeException("No Stripe customer found for this user");
        }

        com.stripe.model.billingportal.Session session = stripeService.createPortalSession(subscription.getStripeCustomerId(), frontendUrl + "/profile");
        
        Map<String, String> response = new HashMap<>();
        response.put("url", session.getUrl());
        return ResponseEntity.ok(response);
    }
}
