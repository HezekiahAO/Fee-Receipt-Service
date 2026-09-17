package com.felxisaf.feereceiptservice.webhooks;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.felxisaf.feereceiptservice.common.exception.BusinessRuleViolationException;
import com.felxisaf.feereceiptservice.common.exception.InvalidWebhookSignatureException;
import com.felxisaf.feereceiptservice.common.exception.ResourceNotFoundException;
import com.felxisaf.feereceiptservice.invoice.Payment;
import com.felxisaf.feereceiptservice.invoice.PaymentRepository;
import com.felxisaf.feereceiptservice.webhooks.dto.WebhookPayload;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;

@Service
public class WebhookService {

    private final PaymentRepository paymentRepository;
    private final ObjectMapper objectMapper;
    private final String webhookSecret;

    public WebhookService(PaymentRepository paymentRepository, ObjectMapper objectMapper,
                           @Value("${app.webhook.secret}") String webhookSecret) {
        this.paymentRepository = paymentRepository;
        this.objectMapper = objectMapper;
        this.webhookSecret = webhookSecret;
    }

    @Transactional
    public void processGatewayWebhook(String rawPayload, String providedSignature) {
        if (!isSignatureValid(rawPayload, providedSignature)) {
            throw new InvalidWebhookSignatureException("Webhook signature verification failed");
        }

        WebhookPayload payload;
        try {
            payload = objectMapper.readValue(rawPayload, WebhookPayload.class);
        } catch (Exception e) {
            throw new BusinessRuleViolationException("Malformed webhook payload");
        }

        Payment payment = paymentRepository.findByPaymentReference(payload.getPaymentReference())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No payment found with reference: " + payload.getPaymentReference()));

        payment.confirmGatewayReference(payload.getGatewayReference());
        paymentRepository.save(payment);
    }

    private boolean isSignatureValid(String rawPayload, String providedSignature) {
        if (providedSignature == null || providedSignature.isBlank()) {
            return false;
        }
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(webhookSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] computedBytes = mac.doFinal(rawPayload.getBytes(StandardCharsets.UTF_8));
            String computedHex = HexFormat.of().formatHex(computedBytes);

            // Constant-time comparison -- avoids leaking timing information about how much
            // of the signature matched, which a naive .equals() comparison would not protect against.
            return MessageDigest.isEqual(
                    computedHex.getBytes(StandardCharsets.UTF_8),
                    providedSignature.getBytes(StandardCharsets.UTF_8)
            );
        } catch (Exception e) {
            return false;
        }
    }
}