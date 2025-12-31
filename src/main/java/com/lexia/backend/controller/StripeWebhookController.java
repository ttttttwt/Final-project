package com.lexia.backend.controller;

import com.lexia.backend.service.SubscriptionService;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.StripeObject;
import com.stripe.model.checkout.Session;
import com.stripe.model.Invoice;
import com.stripe.model.Subscription;
import com.stripe.net.Webhook;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/webhook/stripe")
@RequiredArgsConstructor
@Slf4j
public class StripeWebhookController {

    @Value("${stripe.webhook-secret}")
    private String endpointSecret;

    private final SubscriptionService subscriptionService;

    @PostMapping
    public ResponseEntity<String> handleStripeEvent(@RequestBody String payload, @RequestHeader("Stripe-Signature") String sigHeader) {
        log.info("Received Stripe webhook event");
        Event event;

        try {
            event = Webhook.constructEvent(payload, sigHeader, endpointSecret);
        } catch (SignatureVerificationException e) {
            log.error("Invalid signature", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid signature");
        } catch (Exception e) {
            log.error("Webhook error", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Webhook error");
        }

        StripeObject stripeObject = null;
        if (event.getDataObjectDeserializer().getObject().isPresent()) {
            stripeObject = event.getDataObjectDeserializer().getObject().get();
        } else {
            log.warn("Event data object deserialization failed for event type: {}. API Version: {}. Trying unsafe deserialization.", event.getType(), event.getApiVersion());
            try {
                stripeObject = event.getDataObjectDeserializer().deserializeUnsafe();
            } catch (Exception e) {
                log.error("Unsafe deserialization failed", e);
            }
        }
        
        if (stripeObject == null) {
             log.warn("Stripe object is null even after unsafe deserialization");
             return ResponseEntity.ok("Received");
        }

        log.info("Processing event type: {}", event.getType());

        switch (event.getType()) {
            case "checkout.session.completed":
                Session session = (Session) stripeObject;
                log.info("Handling checkout.session.completed for session: {}", session.getId());
                subscriptionService.handleCheckoutSessionCompleted(session);
                break;
            case "invoice.payment_succeeded":
                Invoice invoice = (Invoice) stripeObject;
                log.info("Handling invoice.payment_succeeded for invoice: {}", invoice.getId());
                subscriptionService.handleInvoicePaymentSucceeded(invoice);
                break;
            case "customer.subscription.deleted":
                Subscription subscription = (Subscription) stripeObject;
                log.info("Handling customer.subscription.deleted for subscription: {}", subscription.getId());
                subscriptionService.handleSubscriptionDeleted(subscription);
                break;
            default:
                log.info("Unhandled event type: {}", event.getType());
                break;
        }

        return ResponseEntity.ok("Received");
    }
}
