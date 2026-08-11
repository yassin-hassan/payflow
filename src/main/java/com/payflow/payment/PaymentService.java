package com.payflow.payment;

import com.payflow.common.ResourceNotFoundException;
import com.payflow.invoice.Invoice;
import com.payflow.invoice.InvoiceRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final InvoiceRepository invoiceRepository;

    public PaymentService(PaymentRepository paymentRepository, InvoiceRepository invoiceRepository) {
        this.paymentRepository = paymentRepository;
        this.invoiceRepository = invoiceRepository;
    }

    @Transactional
    public void processPayment(String eventId, UUID invoiceId, BigDecimal amount) {
        if (!paymentRepository.existsByEventId(eventId)) {
            Invoice invoice = invoiceRepository.findById(invoiceId).orElseThrow(
                    () -> new ResourceNotFoundException("Invoice not found: " + invoiceId)
            );
            Payment payment = new Payment(invoice, eventId, amount);
            paymentRepository.save(payment);
            invoice.addPayment(payment);
            invoice.recomputeStatus();
        }
    }

    public boolean isEventProcessed(String eventId) {
        return paymentRepository.existsByEventId(eventId);
    }
}
