package com.example.gateway.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CreatePaymentRequest {

    @JsonProperty("order_id")
    private String orderId;

    @JsonProperty("method")
    private String method;

    @JsonProperty("vpa")
    private String vpa;

    @JsonProperty("card")
    private CardRequest card;

    public CreatePaymentRequest() {
    }

    public CreatePaymentRequest(String orderId, String method, String vpa, CardRequest card) {
        this.orderId = orderId;
        this.method = method;
        this.vpa = vpa;
        this.card = card;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getVpa() {
        return vpa;
    }

    public void setVpa(String vpa) {
        this.vpa = vpa;
    }

    public CardRequest getCard() {
        return card;
    }

    public void setCard(CardRequest card) {
        this.card = card;
    }

    /**
     * Nested card details
     */
    public static class CardRequest {
        @JsonProperty("number")
        private String number;

        @JsonProperty("expiry_month")
        private Integer expiryMonth;

        @JsonProperty("expiry_year")
        private Integer expiryYear;

        @JsonProperty("cvv")
        private String cvv;

        @JsonProperty("holder_name")
        private String holderName;

        public CardRequest() {
        }

        public CardRequest(String number, Integer expiryMonth, Integer expiryYear, String cvv, String holderName) {
            this.number = number;
            this.expiryMonth = expiryMonth;
            this.expiryYear = expiryYear;
            this.cvv = cvv;
            this.holderName = holderName;
        }

        public String getNumber() {
            return number;
        }

        public void setNumber(String number) {
            this.number = number;
        }

        public Integer getExpiryMonth() {
            return expiryMonth;
        }

        public void setExpiryMonth(Integer expiryMonth) {
            this.expiryMonth = expiryMonth;
        }

        public Integer getExpiryYear() {
            return expiryYear;
        }

        public void setExpiryYear(Integer expiryYear) {
            this.expiryYear = expiryYear;
        }

        public String getCvv() {
            return cvv;
        }

        public void setCvv(String cvv) {
            this.cvv = cvv;
        }

        public String getHolderName() {
            return holderName;
        }

        public void setHolderName(String holderName) {
            this.holderName = holderName;
        }
    }
}
