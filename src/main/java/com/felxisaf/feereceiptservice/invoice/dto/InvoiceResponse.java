package com.felxisaf.feereceiptservice.invoice.dto;

import com.felxisaf.feereceiptservice.invoice.Invoice;
import com.felxisaf.feereceiptservice.invoice.InvoiceStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

public class InvoiceResponse {

    private final Long id;
    private final String invoiceNumber;
    private final Long studentId;
    private final String currency;
    private final BigDecimal totalAmount;
    private final InvoiceStatus status;
    private final LocalDate dueDate;
    private final OffsetDateTime issuedAt;
    private final OffsetDateTime createdAt;
    private final List<InvoiceItemResponse> items;

    public InvoiceResponse(Long id, String invoiceNumber, Long studentId, String currency,
                            BigDecimal totalAmount, InvoiceStatus status, LocalDate dueDate,
                            OffsetDateTime issuedAt, OffsetDateTime createdAt,
                            List<InvoiceItemResponse> items) {
        this.id = id;
        this.invoiceNumber = invoiceNumber;
        this.studentId = studentId;
        this.currency = currency;
        this.totalAmount = totalAmount;
        this.status = status;
        this.dueDate = dueDate;
        this.issuedAt = issuedAt;
        this.createdAt = createdAt;
        this.items = items;
    }

    public static InvoiceResponse fromEntity(Invoice invoice) {
        List<InvoiceItemResponse> itemResponses = invoice.getItems().stream()
                .map(InvoiceItemResponse::fromEntity)
                .toList();

        return new InvoiceResponse(
                invoice.getId(),
                invoice.getInvoiceNumber(),
                invoice.getStudent().getId(),
                invoice.getCurrency(),
                invoice.getTotalAmount(),
                invoice.getStatus(),
                invoice.getDueDate(),
                invoice.getIssuedAt(),
                invoice.getCreatedAt(),
                itemResponses
        );
    }

    public Long getId() {
        return id;
    }

    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public Long getStudentId() {
        return studentId;
    }

    public String getCurrency() {
        return currency;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public InvoiceStatus getStatus() {
        return status;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public OffsetDateTime getIssuedAt() {
        return issuedAt;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public List<InvoiceItemResponse> getItems() {
        return items;
    }
}