package com.felxisaf.feereceiptservice.invoice.dto;


import com.felxisaf.feereceiptservice.invoice.InvoiceStatus;

import java.math.BigDecimal;

public class InvoiceBalanceResponse {

    private final Long invoiceId;
    private final String invoiceNumber;
    private final String currency;
    private final BigDecimal totalAmount;
    private final BigDecimal amountPaid;
    private final BigDecimal outstandingBalance;
    private final InvoiceStatus status;

    public InvoiceBalanceResponse(Long invoiceId, String invoiceNumber, String currency,
                                   BigDecimal totalAmount, BigDecimal amountPaid,
                                   BigDecimal outstandingBalance, InvoiceStatus status) {
        this.invoiceId = invoiceId;
        this.invoiceNumber = invoiceNumber;
        this.currency = currency;
        this.totalAmount = totalAmount;
        this.amountPaid = amountPaid;
        this.outstandingBalance = outstandingBalance;
        this.status = status;
    }

    public Long getInvoiceId() {
        return invoiceId;
    }

    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public String getCurrency() {
        return currency;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public BigDecimal getAmountPaid() {
        return amountPaid;
    }

    public BigDecimal getOutstandingBalance() {
        return outstandingBalance;
    }

    public InvoiceStatus getStatus() {
        return status;
    }
}