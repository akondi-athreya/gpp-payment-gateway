package com.example.gateway.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import java.util.Map;

public class CreateOrderRequest {

    @NotNull(message = "Amount is required")
    @Min(value = 100, message = "Amount must be at least 100 paise (₹1)")
    private Integer amount;

    @JsonProperty("currency")
    private String currency;

    @JsonProperty("receipt")
    private String receipt;

    @JsonProperty("notes")
    private Map<String, Object> notes;

    public CreateOrderRequest() {
    }

    public CreateOrderRequest(Integer amount, String currency, String receipt, Map<String, Object> notes) {
        this.amount = amount;
        this.currency = currency != null ? currency : "INR";
        this.receipt = receipt;
        this.notes = notes;
    }

    public Integer getAmount() {
        return amount;
    }

    public void setAmount(Integer amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency != null ? currency : "INR";
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getReceipt() {
        return receipt;
    }

    public void setReceipt(String receipt) {
        this.receipt = receipt;
    }

    public Map<String, Object> getNotes() {
        return notes;
    }

    public void setNotes(Map<String, Object> notes) {
        this.notes = notes;
    }
}
