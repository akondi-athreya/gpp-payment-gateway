package com.example.gateway.dto;

public class ErrorResponse {
    private Error error;

    public ErrorResponse(String code, String description) {
        this.error = new Error(code, description);
    }

    public static class Error {
        private String code;
        private String description;

        public Error(String code, String description) {
            this.code = code;
            this.description = description;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }
    }

    public Error getError() {
        return error;
    }

    public void setError(Error error) {
        this.error = error;
    }
}
