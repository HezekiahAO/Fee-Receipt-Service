package com.felxisaf.feereceiptservice.invoice;

import com.felxisaf.feereceiptservice.common.exception.BusinessRuleViolationException;
import com.felxisaf.feereceiptservice.common.exception.DuplicateResourceException;
import com.felxisaf.feereceiptservice.common.exception.IdempotencyConflictException;
import com.felxisaf.feereceiptservice.common.exception.ResourceNotFoundException;
import com.felxisaf.feereceiptservice.invoice.dto.PaymentRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Optional;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final InvoiceRepository invoiceRepository;

    public PaymentService(PaymentRepository paymentRepository, InvoiceRepository invoiceRepository) {
        this.paymentRepository = paymentRepository;
        this.invoiceRepository = invoiceRepository;
    }

    @Transactional              // idempotency is handled at the service layer, so we need a transaction here to ensure atomicity.
    public Payment recordPayment(String idempotencyKey, PaymentRequest request) {
        String checksum = computeChecksum(request);

        // Step 1: has this exact idempotency key been used before?
        Optional<Payment> existing = paymentRepository.findByIdempotencyKey(idempotencyKey);
        if (existing.isPresent()) {
            Payment existingPayment = existing.get();
            if (existingPayment.getChecksum().equals(checksum)) {
                // Same key, same request body -> this is a safe retry.
                // Return the original result, do NOT create a second payment.
                return existingPayment;
            } else {
                // Same key, different request body -> the client is misusing the key.
                throw new IdempotencyConflictException(
                        "Idempotency key '" + idempotencyKey + "' was already used with a different request");
            }
        }

        // Step 2: reject a payment_reference we've already recorded (independent of idempotency key)
        if (paymentRepository.existsByPaymentReference(request.getPaymentReference())) {
            throw new DuplicateResourceException(
                    "A payment with paymentReference '" + request.getPaymentReference() + "' already exists");
        }

        // Step 3: load the invoice being paid
        Invoice invoice = invoiceRepository.findById(request.getInvoiceId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Invoice not found with id: " + request.getInvoiceId()));

        if (invoice.getStatus() != InvoiceStatus.ISSUED && invoice.getStatus() != InvoiceStatus.PARTIALLY_PAID) {
            throw new BusinessRuleViolationException(
                    "Payments can only be recorded against an ISSUED or PARTIALLY_PAID invoice. Current status: "
                            + invoice.getStatus());
        }


        // Step 3.5: load the invoice being paid and check currency matches

        if (!request.getCurrency().equalsIgnoreCase(invoice.getCurrency())) {
            throw new BusinessRuleViolationException(
                    "Payment currency '" + request.getCurrency()
                            + "' does not match invoice currency '" + invoice.getCurrency() + "'");
        }


        // Step 4: compute outstanding balance and reject overpayment
        BigDecimal alreadyPaid = sumPayments(invoice.getId());
        BigDecimal outstandingBalance = invoice.getTotalAmount().subtract(alreadyPaid);

        if (request.getAmount().compareTo(outstandingBalance) > 0) {
            throw new BusinessRuleViolationException(
                    "Payment amount " + request.getAmount()
                            + " exceeds the outstanding balance of " + outstandingBalance);
        }

        // Step 5: record the payment
        Payment payment = new Payment(
                request.getPaymentReference(),
                invoice,
                request.getAmount(),
                request.getCurrency().toUpperCase(),
                request.getPaymentMethod(),
                request.getGatewayReference(),
                idempotencyKey,
                checksum
        );
        Payment savedPayment = paymentRepository.save(payment);

        // Step 6: update the invoice's status based on the new total paid
        BigDecimal newTotalPaid = alreadyPaid.add(request.getAmount());
        invoice.updateStatusAfterPayment(newTotalPaid);
        invoiceRepository.save(invoice);

        return savedPayment;
    }

    @Transactional(readOnly = true)
    public Payment getPaymentById(Long id) {
        return paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with id: " + id));
    }

    private BigDecimal sumPayments(Long invoiceId) {
        List<Payment> payments = paymentRepository.findByInvoiceId(invoiceId);
        BigDecimal total = BigDecimal.ZERO;
        for (Payment payment : payments) {
            total = total.add(payment.getAmount());
        }
        return total;
    }

    private String computeChecksum(PaymentRequest request) {
        String canonical = String.join("|",
                request.getPaymentReference(),
                String.valueOf(request.getInvoiceId()),
                request.getAmount().stripTrailingZeros().toPlainString(),
                request.getCurrency().toUpperCase(),
                request.getPaymentMethod(),
                request.getGatewayReference() == null ? "" : request.getGatewayReference()
        );

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(canonical.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : hashBytes) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            // SHA-256 is always available on any standard JVM; this should never happen.
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        }
    }
}