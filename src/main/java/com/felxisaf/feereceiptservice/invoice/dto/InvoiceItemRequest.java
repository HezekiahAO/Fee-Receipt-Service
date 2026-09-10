package com.felxisaf.feereceiptservice.invoice.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public class InvoiceItemRequest {

    @NotBlank(message = "description is required")
    @Size(max = 255, message = "description must be at most 255 characters")
    private String description;

    @NotNull(message = "quantity is required")
    @DecimalMin(value = "0.0001", message = "quantity must be greater than 0")
    private BigDecimal quantity;

    @NotNull(message = "unitAmount is required")
    @DecimalMin(value = "0.0", message = "unitAmount must be zero or greater")
    private BigDecimal unitAmount;

    public InvoiceItemRequest() {
        // required for JSON deserialization
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getUnitAmount() {
        return unitAmount;
    }

    public void setUnitAmount(BigDecimal unitAmount) {
        this.unitAmount = unitAmount;
    }
}