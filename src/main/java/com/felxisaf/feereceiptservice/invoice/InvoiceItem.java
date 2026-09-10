package com.felxisaf.feereceiptservice.invoice;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "invoice_items")
public class InvoiceItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id", nullable = false)
    private Invoice invoice;

    @Column(name = "description", nullable = false, length = 255)
    private String description;

    @Column(name = "quantity", nullable = false, precision = 19, scale = 4)
    private BigDecimal quantity;

    @Column(name = "unit_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal unitAmount;

    @Column(name = "line_total", nullable = false, precision = 19, scale = 4)
    private BigDecimal lineTotal;

    protected InvoiceItem() {
        // required by JPA
    }

    public InvoiceItem(String description, BigDecimal quantity, BigDecimal unitAmount) {
        this.description = description;
        this.quantity = quantity;
        this.unitAmount = unitAmount;
        this.lineTotal = quantity.multiply(unitAmount);
    }

    // Getters and setters

    public Long getId() {
        return id;
    }

    public Invoice getInvoice() {
        return invoice;
    }

    public void setInvoice(Invoice invoice) {
        this.invoice = invoice;
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