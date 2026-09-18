package com.felxisaf.feereceiptservice.reports.dto;

import com.felxisaf.feereceiptservice.invoice.dto.PaymentResponse;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class ReconciliationReportResponse {

    private final LocalDate periodStart;
    private final LocalDate periodEnd;
    private final int totalPaymentCount;
    private final Map<String, BigDecimal> totalAmountByCurrency;
    private final int reconciledCount;
    private final int unreconciledCount;
    private final List<PaymentResponse> unreconciledPayments;

    public ReconciliationReportResponse(LocalDate periodStart, LocalDate periodEnd, int totalPaymentCount,
                                         Map<String, BigDecimal> totalAmountByCurrency, int reconciledCount,
                                         int unreconciledCount, List<PaymentResponse> unreconciledPayments) {
        this.periodStart = periodStart;
        this.periodEnd = periodEnd;
        this.totalPaymentCount = totalPaymentCount;
        this.totalAmountByCurrency = totalAmountByCurrency;
        this.reconciledCount = reconciledCount;
        this.unreconciledCount = unreconciledCount;
        this.unreconciledPayments = unreconciledPayments;
    }

    public LocalDate getPeriodStart() {
        return periodStart;
    }

    public LocalDate getPeriodEnd() {
        return periodEnd;
    }

    public int getTotalPaymentCount() {
        return totalPaymentCount;
    }

    public Map<String, BigDecimal> getTotalAmountByCurrency() {
        return totalAmountByCurrency;
    }

    public int getReconciledCount() {
        return reconciledCount;
    }

    public int getUnreconciledCount() {
        return unreconciledCount;
    }

    public List<PaymentResponse> getUnreconciledPayments() {
        return unreconciledPayments;
    }
}