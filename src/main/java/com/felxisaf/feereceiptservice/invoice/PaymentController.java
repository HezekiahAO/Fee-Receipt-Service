package com.felxisaf.feereceiptservice.invoice;

import com.felxisaf.feereceiptservice.invoice.dto.PaymentRequest;
import com.felxisaf.feereceiptservice.invoice.dto.PaymentResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping
    public ResponseEntity<PaymentResponse> createPayment(
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @Valid @RequestBody PaymentRequest request) {

        Payment payment = paymentService.recordPayment(idempotencyKey, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(PaymentResponse.fromEntity(payment));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaymentResponse> getPayment(@PathVariable Long id) {
        Payment payment = paymentService.getPaymentById(id);
        return ResponseEntity.ok(PaymentResponse.fromEntity(payment));
    }
}