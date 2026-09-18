package com.felxisaf.feereceiptservice.webhooks.dto;

public class WebhookPayload {

    private String paymentReference;
    private String gatewayReference;
    private String status;

    public WebhookPayload() {
        // required for Jackson deserialization
    }

    public String getPaymentReference() {
        return paymentReference;
    }

    public void setPaymentReference(String paymentReference) {
        this.paymentReference = paymentReference;
    }

    public String getGatewayReference() {
        return gatewayReference;
    }

    public void setGatewayReference(String gatewayReference) {
        this.gatewayReference = gatewayReference;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}