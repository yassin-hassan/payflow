package com.payflow.invoice;

import java.math.BigDecimal;

public record CreateInvoiceRequest(BigDecimal amount) {
}
