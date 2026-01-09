package com.example.gateway.models;

import java.time.OffsetDateTime;

import jakarta.persistence.*;

@Entity
@Table(name = "payments", indexes = {
    @Index(name = "idx_payments_order_id", columnList = "order_id"),
    @Index(name = "idx_payments_merchant_id", columnList = "merchant_id"),
    @Index(name = "idx_payments_status", columnList = "status")
})
public class Payment {
    
    @Id
    @Column(nullable = false, length = 64)
    private String id;
    
    @ManyToOne
    @JoinColumn(name = "order_id", referencedColumnName = "id", nullable = false)
    private Order order;
    
    @ManyToOne
    @JoinColumn(name = "merchant_id", referencedColumnName = "id", nullable = false)
    private Merchant merchant;
    
    @Column(nullable = false)
    private int amount;
    
    @Column(nullable = false, length = 3)
    private String currency = "INR";
    
    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private PaymentMethod method;
    
    @Column(nullable = false, length = 20)
    private String status = "created";
    
    @Column(length = 255)
    private String vpa;
    
    @Column(name = "card_network", length = 20)
    @Enumerated(EnumType.STRING)
    private CardNetwork cardNetwork;
    
    @Column(name = "card_last4", length = 4)
    private String cardLast4;
    
    @Column(name = "error_code", length = 50)
    private String errorCode;
    
    @Column(name = "error_description")
    private String errorDescription;

    @Column(
        name = "created_at",
        nullable = false,
        updatable = false,
        columnDefinition = "TIMESTAMPTZ"
    )
    private OffsetDateTime createdAt;

    @Column(
        name = "updated_at",
        nullable = false,
        columnDefinition = "TIMESTAMPTZ"
    )
    private OffsetDateTime updatedAt;

    public Payment() {
    }

    public Payment(String id, Order order, Merchant merchant, int amount, String currency, PaymentMethod method,
            String status, String vpa, CardNetwork cardNetwork, String cardLast4, String errorCode,
            String errorDescription, OffsetDateTime createdAt, OffsetDateTime updatedAt) {
        this.id = id;
        this.order = order;
        this.merchant = merchant;
        this.amount = amount;
        this.currency = currency;
        this.method = method;
        this.status = status;
        this.vpa = vpa;
        this.cardNetwork = cardNetwork;
        this.cardLast4 = cardLast4;
        this.errorCode = errorCode;
        this.errorDescription = errorDescription;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    @PrePersist
    protected void onCreate() {
        OffsetDateTime now = OffsetDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = OffsetDateTime.now();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public Merchant getMerchant() {
        return merchant;
    }

    public void setMerchant(Merchant merchant) {
        this.merchant = merchant;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public PaymentMethod getMethod() {
        return method;
    }

    public void setMethod(PaymentMethod method) {
        this.method = method;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getVpa() {
        return vpa;
    }

    public void setVpa(String vpa) {
        this.vpa = vpa;
    }

    public CardNetwork getCardNetwork() {
        return cardNetwork;
    }

    public void setCardNetwork(CardNetwork cardNetwork) {
        this.cardNetwork = cardNetwork;
    }

    public String getCardLast4() {
        return cardLast4;
    }

    public void setCardLast4(String cardLast4) {
        this.cardLast4 = cardLast4;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorDescription() {
        return errorDescription;
    }

    public void setErrorDescription(String errorDescription) {
        this.errorDescription = errorDescription;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(OffsetDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "Payment [id=" + id + ", order=" + (order != null ? order.getId() : null) 
                + ", merchant=" + (merchant != null ? merchant.getId() : null) + ", amount=" + amount
                + ", currency=" + currency + ", method=" + method + ", status=" + status + ", vpa=" + vpa
                + ", cardNetwork=" + cardNetwork + ", cardLast4=" + cardLast4 + ", errorCode=" + errorCode
                + ", errorDescription=" + errorDescription + ", createdAt=" + createdAt + ", updatedAt=" + updatedAt
                + "]";
    }

}