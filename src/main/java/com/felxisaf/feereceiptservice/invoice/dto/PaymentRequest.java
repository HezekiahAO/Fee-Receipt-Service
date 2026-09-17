package com.felxisaf.feereceiptservice.invoice.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public class PaymentRequest {

    @NotBlank(message = "paymentReference is required")
    @Size(max = 100, message = "paymentReference must be at most 100 characters")
    private String paymentReference;

    @NotNull(message = "invoiceId is required")
    private Long invoiceId;

    @NotNull(message = "amount is required")
    @DecimalMin(value = "0.0001", message = "amount must be greater than 0")
    private BigDecimal amount;

    @NotBlank(message = "currency is required")
    @Size(min = 3, max = 3, message = "currency must be a 3-letter code, e.g. USD")
    private String currency;

    @NotBlank(message = "paymentMethod is required")
    @Size(max = 30, message = "paymentMethod must be at most 30 characters")
    private String paymentMethod;

    @Size(max = 100, message = "gatewayReference must be at most 100 characters")
    private String gatewayReference;

    public PaymentRequest() {
        // required for JSON deserialization
    }

    public String getPaymentReference() {
        return paymentReference;
    }

    public void setPaymentReference(String paymentReference) {
        this.paymentReference = paymentReference;
    }

    public Long getInvoiceId() {
        return invoiceId;
    }

    public void setInvoiceId(Long invoiceId) {
        this.invoiceId = invoiceId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getGatewayReference() {
        return gatewayReference;
    }

    public void setGatewayReference(String gatewayReference) {
        this.gatewayReference = gatewayReference;
    }
}