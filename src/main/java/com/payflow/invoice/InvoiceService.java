package com.payflow.invoice;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class InvoiceService {
    private final InvoiceRepository invoiceRepository;

    public InvoiceService(InvoiceRepository invoiceRepository) {
        this.invoiceRepository = invoiceRepository;
    }

    public Invoice createInvoice(BigDecimal amount) {
        Invoice invoice = new Invoice(amount);
        return invoiceRepository.save(invoice);
    }
}
