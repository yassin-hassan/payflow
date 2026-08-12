package com.payflow.payment;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PaymentWebhookController {
    private PaymentService paymentService;

    public PaymentWebhookController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/api/webhooks/payment")
    public ResponseEntity<Void> receivePaymentWebhook(@RequestBody PaymentWebhookRequest request) {
        int maxAttempts = 3;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                paymentService.processPayment(request.eventId(), request.invoiceId(), request.amount());
                return ResponseEntity.ok().build();
            } catch (ObjectOptimisticLockingFailureException e) {
                if (attempt == maxAttempts) throw e;
            } catch (DataIntegrityViolationException e) {
                // concurrent duplicate : another thread recorded this event -> already handled
                if (!paymentService.isEventProcessed(request.eventId())) throw e;
                // duplicate means event is already handled send a 200 for idempotency
                return ResponseEntity.ok().build();
            }
        }
        // purely to satisfy compiler
        throw new IllegalStateException("unreachable");
    }

    @PostMapping("/api/webhooks/payment-naive")
    public ResponseEntity<Void> receivePaymentWebhookNaive(@RequestBody PaymentWebhookRequest request) {
        paymentService.processPayment(request.eventId(), request.invoiceId(), request.amount());
        return ResponseEntity.ok().build();
    }
}
