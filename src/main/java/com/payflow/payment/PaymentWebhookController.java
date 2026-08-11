package com.payflow.payment;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
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
        try {
            paymentService.processPayment(request.eventId(), request.invoiceId(), request.amount());
        } catch (DataIntegrityViolationException e) {
            // concurrent duplicate : another thread recorded this event -> already handled
            if (!paymentService.isEventProcessed(request.eventId())) {
                throw e;
            }
        }
        return ResponseEntity.ok().build();
    }
}
