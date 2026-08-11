package com.payflow.invoice;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class InvoiceController {

    private final InvoiceService invoiceService;

    public InvoiceController(InvoiceService invoiceService) {
        this.invoiceService = invoiceService;
    }

    @PostMapping("/api/invoices")
    public ResponseEntity<InvoiceResponse> create(@RequestBody CreateInvoiceRequest request) {
        Invoice invoice = invoiceService.createInvoice(request.amount());
        InvoiceResponse response = new InvoiceResponse(invoice.getId(), invoice.getAmount(), invoice.getStatus());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
