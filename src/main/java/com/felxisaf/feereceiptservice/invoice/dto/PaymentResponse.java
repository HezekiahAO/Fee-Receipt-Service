package com.felxisaf.feereceiptservice.invoice.dto;

import com.felxisaf.feereceiptservice.invoice.Payment;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public class PaymentResponse {

    private final Long id;
    private final String paymentReference;
    private final Long invoiceId;
    private final BigDecimal amount;
    private final String currency;
    private final String paymentMethod;
    private final String gatewayReference;
    private final OffsetDateTime paidAt;

    public PaymentResponse(Long id, String paymentReference, Long invoiceId, BigDecimal amount,
                            String currency, String paymentMethod, String gatewayReference,
                            OffsetDateTime paidAt) {
        this.id = id;
        this.paymentReference = paymentReference;
        this.invoiceId = invoiceId;
        this.amount = amount;
        this.currency = currency;
        this.paymentMethod = paymentMethod;
        this.gatewayReference = gatewayReference;
        this.paidAt = paidAt;
    }

    public static PaymentResponse fromEntity(Payment payment) {
        return new PaymentResponse(
                payment.getId(),
                payment.getPaymentReference(),
                payment.getInvoice().getId(),
                payment.getAmount(),
                payment.getCurrency(),
                payment.getPaymentMethod(),
                payment.getGatewayReference(),
                payment.getPaidAt()
        );
    }

    public Long getId() {
        return id;
    }

    public String getPaymentReference() {
        return paymentReference;
    }

    public Long getInvoiceId() {
        return invoiceId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public String getGatewayReference() {
        return gatewayReference;
    }

    public OffsetDateTime getPaidAt() {
        return paidAt;
    }
}