package com.payflow.payment;

import com.payflow.invoice.Invoice;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "payments")
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id")
    private Invoice invoice;

    @Column(name = "event_id")
    private String eventId;

    private BigDecimal amount;

    protected Payment(){}

    public Payment(Invoice invoice, String eventId, BigDecimal amount) {
        this.invoice = invoice;
        this.eventId = eventId;
        this.amount = amount;
    }

    public BigDecimal getAmount() {
        return amount;
    }

}
