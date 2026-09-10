package com.felxisaf.feereceiptservice.invoice.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.util.List;

public class InvoiceRequest {

    @NotBlank(message = "invoiceNumber is required")
    @Size(max = 50, message = "invoiceNumber must be at most 50 characters")
    private String invoiceNumber;

    @NotNull(message = "studentId is required")
    private Long studentId;

    @NotBlank(message = "currency is required")
    @Size(min = 3, max = 3, message = "currency must be a 3-letter code, e.g. USD")
    private String currency;

    @NotNull(message = "dueDate is required")
    @FutureOrPresent(message = "dueDate cannot be in the past")
    private LocalDate dueDate;

    @NotEmpty(message = "an invoice must have at least one line item")
    @Valid
    private List<InvoiceItemRequest> items;

    public InvoiceRequest() {
        // required for JSON deserialization
    }

    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public List<InvoiceItemRequest> getItems() {
        return items;
    }

    public void setItems(List<InvoiceItemRequest> items) {
        this.items = items;
    }
}