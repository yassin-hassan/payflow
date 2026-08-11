package com.payflow.payment;

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

    @PostMapping("api/webhooks/payment")
    public ResponseEntity<Void> receivePaymentWebhook(@RequestBody PaymentWebhookRequest request) {
        paymentService.processPayment(request.eventId(), request.invoiceId(), request.amount());
        return ResponseEntity.ok().build();
    }
}
