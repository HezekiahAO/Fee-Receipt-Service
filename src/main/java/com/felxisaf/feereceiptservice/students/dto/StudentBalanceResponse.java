package com.felxisaf.feereceiptservice.students.dto;

import com.felxisaf.feereceiptservice.invoice.dto.InvoiceBalanceResponse;

import java.math.BigDecimal;
import java.util.List;

public class StudentBalanceResponse {

    private final Long studentId;
    private final BigDecimal totalInvoiced;
    private final BigDecimal totalPaid;
    private final BigDecimal totalOutstanding;
    private final List<InvoiceBalanceResponse> invoices;

    public StudentBalanceResponse(Long studentId, BigDecimal totalInvoiced, BigDecimal totalPaid,
                                   BigDecimal totalOutstanding, List<InvoiceBalanceResponse> invoices) {
        this.studentId = studentId;
        this.totalInvoiced = totalInvoiced;
        this.totalPaid = totalPaid;
        this.totalOutstanding = totalOutstanding;
        this.invoices = invoices;
    }

    public Long getStudentId() {
        return studentId;
    }

    public BigDecimal getTotalInvoiced() {
        return totalInvoiced;
    }

    public BigDecimal getTotalPaid() {
        return totalPaid;
    }

    public BigDecimal getTotalOutstanding() {
        return totalOutstanding;
    }

    public List<InvoiceBalanceResponse> getInvoices() {
        return invoices;
    }
}