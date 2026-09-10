package com.felxisaf.feereceiptservice.invoice.dto;

import com.felxisaf.feereceiptservice.invoice.InvoiceItem;

import java.math.BigDecimal;

public class InvoiceItemResponse {

    private final Long id;
    private final String description;
    private final BigDecimal quantity;
    private final BigDecimal unitAmount;
    private final BigDecimal lineTotal;

    public InvoiceItemResponse(Long id, String description, BigDecimal quantity,
                                BigDecimal unitAmount, BigDecimal lineTotal) {
        this.id = id;
        this.description = description;
        this.quantity = quantity;
        this.unitAmount = unitAmount;
        this.lineTotal = lineTotal;
    }

    public static InvoiceItemResponse fromEntity(InvoiceItem item) {
        return new InvoiceItemResponse(
                item.getId(),
                item.getDescription(),
                item.getQuantity(),
                item.getUnitAmount(),
                item.getLineTotal()
        );
    }

    public Long getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public BigDecimal getUnitAmount() {
        return unitAmount;
    }

    public BigDecimal getLineTotal() {
        return lineTotal;
    }
}