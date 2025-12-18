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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/webhook/stripe")
@RequiredArgsConstructor
public class StripeWebhookController {

    @Value("${stripe.webhook-secret}")
    private String endpointSecret;

    private final SubscriptionService subscriptionService;

    @PostMapping
    public ResponseEntity<String> handleStripeEvent(@RequestBody String payload, @RequestHeader("Stripe-Signature") String sigHeader) {
        Event event;

        try {
            event = Webhook.constructEvent(payload, sigHeader, endpointSecret);
        } catch (SignatureVerificationException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid signature");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Webhook error");
        }

        StripeObject stripeObject = event.getDataObjectDeserializer().getObject().orElse(null);
        
        if (stripeObject == null) {
             return ResponseEntity.ok("Received");
        }

        switch (event.getType()) {
            case "checkout.session.completed":
                Session session = (Session) stripeObject;
                subscriptionService.handleCheckoutSessionCompleted(session);
                break;
            case "invoice.payment_succeeded":
                Invoice invoice = (Invoice) stripeObject;
                subscriptionService.handleInvoicePaymentSucceeded(invoice);
                break;
            case "customer.subscription.deleted":
                Subscription subscription = (Subscription) stripeObject;
                subscriptionService.handleSubscriptionDeleted(subscription);
                break;
            default:
                // Handle other event types
                break;
        }

        return ResponseEntity.ok("Received");
    }
}
