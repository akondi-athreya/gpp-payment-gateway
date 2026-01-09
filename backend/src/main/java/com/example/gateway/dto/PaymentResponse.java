package com.example.gateway.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.OffsetDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class PaymentResponse {

    @JsonProperty("id")
    private String id;

    @JsonProperty("order_id")
    private String orderId;

    @JsonProperty("merchant_id")
    private String merchantId;

    @JsonProperty("amount")
    private Integer amount;

    @JsonProperty("currency")
    private String currency;

    @JsonProperty("method")
    private String method;

    @JsonProperty("status")
    private String status;

    @JsonProperty("vpa")
    private String vpa;

    @JsonProperty("card_network")
    private String cardNetwork;

    @JsonProperty("card_last4")
    private String cardLast4;

    @JsonProperty("error_code")
    private String errorCode;

    @JsonProperty("error_description")
    private String errorDescription;

    @JsonProperty("created_at")
    private OffsetDateTime createdAt;

    @JsonProperty("updated_at")
    private OffsetDateTime updatedAt;

    public PaymentResponse() {
    }

    public PaymentResponse(String id, String orderId, String merchantId, Integer amount, String currency,
                          String method, String status, String vpa, String cardNetwork, String cardLast4,
                          String errorCode, String errorDescription, OffsetDateTime createdAt, OffsetDateTime updatedAt) {
        this.id = id;
        this.orderId = orderId;
        this.merchantId = merchantId;
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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(String merchantId) {
        this.merchantId = merchantId;
    }

    public Integer getAmount() {
        return amount;
    }

    public void setAmount(Integer amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
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

    public String getCardNetwork() {
        return cardNetwork;
    }

    public void setCardNetwork(String cardNetwork) {
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
}
