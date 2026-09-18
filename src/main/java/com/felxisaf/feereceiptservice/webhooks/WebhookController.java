package com.felxisaf.feereceiptservice.webhooks;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/webhooks")
public class WebhookController {

    private final WebhookService webhookService;

    public WebhookController(WebhookService webhookService) {
        this.webhookService = webhookService;
    }

    @PostMapping("/gateway")
    public ResponseEntity<Void> handleGatewayWebhook(
            @RequestHeader("X-Webhook-Signature") String signature,
            @RequestBody String rawPayload) {

        webhookService.processGatewayWebhook(rawPayload, signature);
        return ResponseEntity.ok().build();
    }
}