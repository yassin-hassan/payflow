package com.payflow.payment;

import com.payflow.common.ResourceNotFoundException;
import com.payflow.invoice.Invoice;
import com.payflow.invoice.InvoiceRepository;
import com.payflow.invoice.InvoiceStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PaymentServiceTest {
    @Mock
    PaymentRepository paymentRepository;

    @Mock
    InvoiceRepository invoiceRepository;

    @InjectMocks
    PaymentService paymentService;

    @Test
    void newEvent_savesPaymentAndRecomputesStatus() {
        UUID invoiceId = UUID.randomUUID();
        Invoice invoice = new Invoice(new BigDecimal("100.00"));

        // simulations : if these are called then return this
        when(paymentRepository.existsByEventId("evt_1")).thenReturn(false);
        when(invoiceRepository.findById(invoiceId)).thenReturn(Optional.of(invoice));

        paymentService.processPayment("evt_1", invoiceId, new BigDecimal("50.00"));

        // check that "save" was called on this repo with any instance of this class
        verify(paymentRepository).save(any(Payment.class));
        assertThat(invoice.getStatus()).isEqualTo(InvoiceStatus.PARTIAL);
    }

    @Test
    void duplicateEvent_doesNothing() {
        UUID invoiceId = UUID.randomUUID();
        when(paymentRepository.existsByEventId("evt_1")).thenReturn(true);
        paymentService.processPayment("evt_1", invoiceId, new BigDecimal("50.00"));
        verify(paymentRepository, never()).save(any());
        verify(invoiceRepository, never()).findById(any());
    }

    @Test
    void unknownInvoice_throwsNotFound() {
        UUID invoiceId = UUID.randomUUID();

        when(paymentRepository.existsByEventId("evt_1")).thenReturn(false);
        when(invoiceRepository.findById(invoiceId)).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                paymentService.processPayment("evt_1", invoiceId, new BigDecimal("50.00")))
                .isInstanceOf(ResourceNotFoundException.class);

    }
}
