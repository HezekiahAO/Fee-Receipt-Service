package com.felxisaf.feereceiptservice.reports;

import com.felxisaf.feereceiptservice.invoice.Payment;
import com.felxisaf.feereceiptservice.invoice.PaymentRepository;
import com.felxisaf.feereceiptservice.invoice.dto.PaymentResponse;
import com.felxisaf.feereceiptservice.reports.dto.ReconciliationReportResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReportService {

    private final PaymentRepository paymentRepository;

    public ReportService(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    @Transactional(readOnly = true)
    public ReconciliationReportResponse generateReconciliationReport(LocalDate startDate, LocalDate endDate) {
        // Assumption: treating day boundaries in UTC for simplicity.
        // A production system would use the institution's configured timezone instead.
        OffsetDateTime start = startDate.atStartOfDay().atOffset(ZoneOffset.UTC);
        OffsetDateTime end = endDate.plusDays(1).atStartOfDay().atOffset(ZoneOffset.UTC).minusNanos(1);

        List<Payment> payments = paymentRepository.findByPaidAtBetween(start, end);

        List<Payment> reconciled = payments.stream()
                .filter(p -> p.getGatewayReference() != null && !p.getGatewayReference().isBlank())
                .toList();

        List<Payment> unreconciled = payments.stream()
                .filter(p -> p.getGatewayReference() == null || p.getGatewayReference().isBlank())
                .toList();

        // Grouped by currency deliberately -- summing across different currencies
        // into one number would be meaningless, the same mistake we just fixed in PaymentService.
        Map<String, BigDecimal> totalsByCurrency = new LinkedHashMap<>();
        for (Payment payment : payments) {
            totalsByCurrency.merge(payment.getCurrency(), payment.getAmount(), BigDecimal::add);
        }

        List<PaymentResponse> unreconciledResponses = unreconciled.stream()
                .map(PaymentResponse::fromEntity)
                .toList();

        return new ReconciliationReportResponse(
                startDate,
                endDate,
                payments.size(),
                totalsByCurrency,
                reconciled.size(),
                unreconciled.size(),
                unreconciledResponses
        );
    }
}