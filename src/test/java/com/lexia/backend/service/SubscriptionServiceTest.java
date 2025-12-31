package com.lexia.backend.service;

import com.lexia.backend.entity.Payment;
import com.lexia.backend.entity.Subscription;
import com.lexia.backend.entity.User;
import com.lexia.backend.enums.PlanType;
import com.lexia.backend.enums.SubscriptionStatus;
import com.lexia.backend.repository.PaymentRepository;
import com.lexia.backend.repository.SubscriptionRepository;
import com.lexia.backend.repository.UserRepository;
import com.stripe.model.Invoice;
import com.stripe.model.InvoiceLineItem;
import com.stripe.model.InvoiceLineItemCollection;
// import com.stripe.model.InvoiceLineItemPeriod;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubscriptionServiceTest {

    @Mock
    private SubscriptionRepository subscriptionRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private SubscriptionQuotaService subscriptionQuotaService;
    @Mock
    private PaymentRepository paymentRepository;

    @InjectMocks
    private SubscriptionService subscriptionService;

    private Invoice invoice;
    private Subscription subscription;
    private Payment payment;
    private User user;

    @BeforeEach
    void setUp() {
        invoice = mock(Invoice.class);
        user = new User();
        user.setId(UUID.randomUUID());

        subscription = new Subscription();
        subscription.setId(UUID.randomUUID());
        subscription.setUser(user);
        subscription.setStripeSubscriptionId("sub_123");
        subscription.setPlanType(PlanType.MONTHLY);

        payment = new Payment();
        payment.setId(UUID.randomUUID());
        payment.setStripeInvoiceId("in_123");
        payment.setSubscription(subscription);
        payment.setStatus(Payment.PaymentStatus.PENDING);
    }

    @Test
    void handleInvoicePaymentSucceeded_WithNullSubscriptionId_ShouldUseFallback() {
        // Arrange
        when(invoice.getSubscription()).thenReturn(null);
        when(invoice.getId()).thenReturn("in_123");
        
        // Mock payment repository to return payment linked to subscription
        when(paymentRepository.findByStripeInvoiceId("in_123")).thenReturn(Optional.of(payment));
        
        // Mock invoice details for processing
        InvoiceLineItemCollection lines = mock(InvoiceLineItemCollection.class);
        InvoiceLineItem lineItem = mock(InvoiceLineItem.class);
        InvoiceLineItem.Period period = mock(InvoiceLineItem.Period.class);
        
        when(invoice.getLines()).thenReturn(lines);
        when(lines.getData()).thenReturn(Collections.singletonList(lineItem));
        when(lineItem.getPeriod()).thenReturn(period);
        when(period.getEnd()).thenReturn(1735689600L); // 2025-01-01
        
        when(invoice.getAmountPaid()).thenReturn(1000L);
        when(invoice.getCurrency()).thenReturn("usd");
        when(invoice.getBillingReason()).thenReturn("subscription_create");
        when(invoice.getPaymentIntent()).thenReturn("pi_123");

        // Act
        subscriptionService.handleInvoicePaymentSucceeded(invoice);

        // Assert
        verify(subscriptionRepository).save(subscription);
        verify(paymentRepository, times(2)).findByStripeInvoiceId("in_123"); // Once for fallback, once in savePaymentFromInvoice
        verify(paymentRepository).save(any(Payment.class));
        verify(subscriptionQuotaService).syncQuotaWithSubscription(user, subscription);
    }

    @Test
    void handleInvoicePaymentSucceeded_WithNullSubscriptionId_AndNoPayment_ShouldSkip() {
        // Arrange
        when(invoice.getSubscription()).thenReturn(null);
        when(invoice.getId()).thenReturn("in_123");
        when(paymentRepository.findByStripeInvoiceId("in_123")).thenReturn(Optional.empty());

        // Act
        subscriptionService.handleInvoicePaymentSucceeded(invoice);

        // Assert
        verify(subscriptionRepository, never()).save(any());
        verify(subscriptionQuotaService, never()).syncQuotaWithSubscription(any(), any());
    }

    @Test
    void handleChargeRefunded_ShouldDowngradeSubscription() {
        // Arrange
        com.stripe.model.Charge charge = mock(com.stripe.model.Charge.class);
        when(charge.getPaymentIntent()).thenReturn("pi_123");
        
        payment.setStripePaymentId("pi_123");
        payment.setStatus(Payment.PaymentStatus.SUCCEEDED);
        
        when(paymentRepository.findByStripePaymentId("pi_123")).thenReturn(Optional.of(payment));

        // Act
        subscriptionService.handleChargeRefunded(charge);

        // Assert
        verify(paymentRepository).save(payment);
        assert payment.getStatus() == Payment.PaymentStatus.REFUNDED;
        
        verify(subscriptionRepository).save(subscription);
        assert subscription.getStatus() == SubscriptionStatus.CANCELED;
        assert subscription.getPlanType() == PlanType.FREE;
        
        verify(subscriptionQuotaService).syncQuotaWithSubscription(user, subscription);
    }
}
