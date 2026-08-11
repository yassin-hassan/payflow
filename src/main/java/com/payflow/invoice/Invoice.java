package com.payflow.invoice;

import com.payflow.payment.Payment;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "invoices")
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    private InvoiceStatus status = InvoiceStatus.PENDING;;

    @OneToMany(mappedBy = "invoice")
    // lazy fetch by default
    private List<Payment> payments;

    protected Invoice(){}

    public void recomputeStatus() {
        BigDecimal totalAmount = payments.stream()
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalAmount.compareTo(this.amount) >= 0) {
            status = InvoiceStatus.PAID;
        } else {
            status = InvoiceStatus.PARTIAL;
        }
    }

    public void addPayment(Payment payment) {
        payments.add(payment);
    }

}
