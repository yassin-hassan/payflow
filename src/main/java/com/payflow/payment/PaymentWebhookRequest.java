package com.payflow.payment;

import java.math.BigDecimal;
import java.util.UUID;

public record PaymentWebhookRequest(String eventId, BigDecimal amount, UUID invoiceId) {
}
