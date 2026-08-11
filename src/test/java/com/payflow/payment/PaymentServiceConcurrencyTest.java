package com.payflow.payment;

import com.payflow.invoice.Invoice;
import com.payflow.invoice.InvoiceRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
public class PaymentServiceConcurrencyTest {

    @Autowired
    PaymentService paymentService;
    @Autowired
    InvoiceRepository invoiceRepository;
    @Autowired
    PaymentRepository paymentRepository;

    @Test
    void testConcurrentDuplicates_onlyOnePaymentRecorded() throws InterruptedException {
        Invoice invoice = invoiceRepository.save(new Invoice(new BigDecimal("100.00")));
        UUID invoiceId = invoice.getId();
        String eventId = "evt_race";

        int threadCount = 2;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startGate = new CountDownLatch(1);
        CountDownLatch doneGate = new CountDownLatch(threadCount);
        List<Throwable> errors = Collections.synchronizedList(new ArrayList<>());

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    startGate.await();
                    paymentService.processPayment(eventId, invoiceId, new BigDecimal("50.00"));
                } catch (Throwable t) {
                    errors.add(t);
                } finally {
                    doneGate.countDown();
                }
            });
        }

        startGate.countDown();
        doneGate.await();
        executor.shutdown();

        System.out.println(">>> payments recorded: " + paymentRepository.count());
        System.out.println(">>> errors thrown: " + errors);
        assertThat(paymentRepository.count()).isEqualTo(1);
    }
}
