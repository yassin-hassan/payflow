package com.payflow.payment;

import com.payflow.invoice.Invoice;
import com.payflow.invoice.InvoiceRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT)
class PaymentWebhookConcurrencyTest {

    @LocalServerPort int port;
    @Autowired InvoiceRepository invoiceRepository;
    @Autowired PaymentRepository paymentRepository;

    @Test
    void twoConcurrentDuplicates_overHttp() throws InterruptedException {
        Invoice invoice = invoiceRepository.save(new Invoice(new BigDecimal("100.00")));
        String json = "{\"eventId\":\"evt_race_http\",\"amount\":50.00,\"invoiceId\":\"" + invoice.getId() + "\"}";

        int threadCount = 2;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startGate = new CountDownLatch(1);
        CountDownLatch doneGate  = new CountDownLatch(threadCount);
        List<Integer> statuses = Collections.synchronizedList(new ArrayList<>());
        HttpClient client = HttpClient.newHttpClient();

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    startGate.await();
                    HttpRequest req = HttpRequest.newBuilder()
                            .uri(URI.create("http://localhost:" + port + "/api/webhooks/payment-naive"))
                            .header("Content-Type", "application/json")
                            .POST(HttpRequest.BodyPublishers.ofString(json))
                            .build();
                    HttpResponse<Void> res = client.send(req, HttpResponse.BodyHandlers.discarding());
                    statuses.add(res.statusCode());
                } catch (Exception e) {
                    statuses.add(-1);
                } finally {
                    doneGate.countDown();
                }
            });
        }

        startGate.countDown();
        doneGate.await();
        executor.shutdown();

        System.out.println(">>> status codes: " + statuses);
        System.out.println(">>> payments: " + paymentRepository.count());
    }
}