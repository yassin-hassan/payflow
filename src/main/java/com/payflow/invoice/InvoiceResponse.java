package com.payflow.invoice;

import java.math.BigDecimal;
import java.util.UUID;

public record InvoiceResponse(UUID id, BigDecimal amount, InvoiceStatus invoiceStatus) {
}
