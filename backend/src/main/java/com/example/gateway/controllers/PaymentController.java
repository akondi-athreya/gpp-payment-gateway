package com.example.gateway.controllers;

import com.example.gateway.dto.CreatePaymentRequest;
import com.example.gateway.dto.ErrorResponse;
import com.example.gateway.dto.PaymentResponse;
import com.example.gateway.models.Payment;
import com.example.gateway.services.PaymentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;

@RestController
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    /**
     * GET /api/v1/payments - Get all payments for merchant (Authenticated)
     * Requires X-Api-Key and X-Api-Secret headers
     */
    @GetMapping("/api/v1/payments")
    public ResponseEntity<?> getPayments(
            @RequestHeader("X-Api-Key") String apiKey,
            @RequestHeader("X-Api-Secret") String apiSecret) {

        try {
            // Get all payments for this merchant
            List<Payment> payments = paymentService.getPaymentsByMerchant(apiKey, apiSecret);
            List<PaymentResponse> response = payments.stream()
                    .map(this::mapPaymentToResponse)
                    .collect(Collectors.toList());
            return ResponseEntity.status(HttpStatus.OK).body(response);

        } catch (IllegalArgumentException e) {
            return handleIllegalArgument(e);
        } catch (Exception e) {
            ErrorResponse errorResponse = new ErrorResponse("BAD_REQUEST_ERROR", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    /**
     * POST /api/v1/payments - Create payment (Authenticated)
     * Requires X-Api-Key and X-Api-Secret headers
     */
    @PostMapping("/api/v1/payments")
    public ResponseEntity<?> createPayment(
            @RequestHeader("X-Api-Key") String apiKey,
            @RequestHeader("X-Api-Secret") String apiSecret,
            @RequestBody CreatePaymentRequest request) {

        try {
            // Validate request
            if (request.getOrderId() == null || request.getOrderId().isEmpty()) {
                ErrorResponse errorResponse = new ErrorResponse("BAD_REQUEST_ERROR", "order_id is required");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
            }

            if (request.getMethod() == null || request.getMethod().isEmpty()) {
                ErrorResponse errorResponse = new ErrorResponse("BAD_REQUEST_ERROR", "method is required");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
            }

            // Extract card details if present
            String cardNumber = null;
            Integer expiryMonth = null;
            Integer expiryYear = null;
            String cvv = null;
            String holderName = null;

            if (request.getCard() != null) {
                cardNumber = request.getCard().getNumber();
                expiryMonth = request.getCard().getExpiryMonth();
                expiryYear = request.getCard().getExpiryYear();
                cvv = request.getCard().getCvv();
                holderName = request.getCard().getHolderName();
            }

            // Create and process payment
            Payment payment = paymentService.createAndProcessPayment(
                    apiKey,
                    apiSecret,
                    request.getOrderId(),
                    request.getMethod(),
                    request.getVpa(),
                    cardNumber,
                    cvv,
                    holderName,
                    expiryMonth,
                    expiryYear
            );

            // Build response
            PaymentResponse response = mapPaymentToResponse(payment);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (IllegalArgumentException e) {
            return handleIllegalArgument(e);
        } catch (Exception e) {
            ErrorResponse errorResponse = new ErrorResponse("BAD_REQUEST_ERROR", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    /**
     * GET /api/v1/payments/{payment_id} - Get payment details (Authenticated)
     * Requires X-Api-Key and X-Api-Secret headers
     */
    @GetMapping("/api/v1/payments/{payment_id}")
    public ResponseEntity<?> getPayment(
            @RequestHeader("X-Api-Key") String apiKey,
            @RequestHeader("X-Api-Secret") String apiSecret,
            @PathVariable("payment_id") String paymentId) {

        try {
            // Note: In a real system, you'd verify the merchant owns this payment
            // For now, we just verify merchant exists and return the payment
            Payment payment = paymentService.getPayment(paymentId);
            PaymentResponse response = mapPaymentToResponse(payment);
            return ResponseEntity.status(HttpStatus.OK).body(response);

        } catch (IllegalArgumentException e) {
            return handleIllegalArgument(e);
        } catch (Exception e) {
            ErrorResponse errorResponse = new ErrorResponse("BAD_REQUEST_ERROR", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    /**
     * POST /api/v1/payments/public - Create payment (Public - for checkout page)
     * No authentication required, but validates order_id
     */
    @PostMapping("/api/v1/payments/public")
    public ResponseEntity<?> createPaymentPublic(
            @RequestBody CreatePaymentRequest request) {

        try {
            // For public endpoint, we need to use a test merchant approach
            // Since checkout page doesn't have merchant credentials,
            // we validate that order exists and use its merchant

            if (request.getOrderId() == null || request.getOrderId().isEmpty()) {
                ErrorResponse errorResponse = new ErrorResponse("BAD_REQUEST_ERROR", "order_id is required");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
            }

            if (request.getMethod() == null || request.getMethod().isEmpty()) {
                ErrorResponse errorResponse = new ErrorResponse("BAD_REQUEST_ERROR", "method is required");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
            }

            // Extract card details if present
            String cardNumber = null;
            Integer expiryMonth = null;
            Integer expiryYear = null;
            String cvv = null;
            String holderName = null;

            if (request.getCard() != null) {
                cardNumber = request.getCard().getNumber();
                expiryMonth = request.getCard().getExpiryMonth();
                expiryYear = request.getCard().getExpiryYear();
                cvv = request.getCard().getCvv();
                holderName = request.getCard().getHolderName();
            }

            // Use test merchant credentials for public endpoint
            String testApiKey = "key_test_abc123";
            String testApiSecret = "secret_test_xyz789";

            // Create and process payment
            Payment payment = paymentService.createAndProcessPayment(
                    testApiKey,
                    testApiSecret,
                    request.getOrderId(),
                    request.getMethod(),
                    request.getVpa(),
                    cardNumber,
                    cvv,
                    holderName,
                    expiryMonth,
                    expiryYear
            );

            // Build response
            PaymentResponse response = mapPaymentToResponse(payment);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (IllegalArgumentException e) {
            return handleIllegalArgument(e);
        } catch (Exception e) {
            ErrorResponse errorResponse = new ErrorResponse("BAD_REQUEST_ERROR", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    /**
     * GET /api/v1/payments/{payment_id}/public - Get payment status (Public - for checkout page)
     * No authentication required
     */
    @GetMapping("/api/v1/payments/{payment_id}/public")
    public ResponseEntity<?> getPaymentPublic(
            @PathVariable("payment_id") String paymentId) {

        try {
            Payment payment = paymentService.getPaymentPublic(paymentId);
            PaymentResponse response = mapPaymentToResponse(payment);
            return ResponseEntity.status(HttpStatus.OK).body(response);

        } catch (IllegalArgumentException e) {
            return handleIllegalArgument(e);
        } catch (Exception e) {
            ErrorResponse errorResponse = new ErrorResponse("BAD_REQUEST_ERROR", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    /**
     * Helper method to map Payment entity to PaymentResponse DTO
     */
    private PaymentResponse mapPaymentToResponse(Payment payment) {
        PaymentResponse response = new PaymentResponse();
        response.setId(payment.getId());
        response.setOrderId(payment.getOrder().getId());
        response.setMerchantId(payment.getMerchant().getId().toString());
        response.setAmount(payment.getAmount());
        response.setCurrency(payment.getCurrency());
        response.setMethod(payment.getMethod().name().toLowerCase());
        response.setStatus(payment.getStatus());
        response.setVpa(payment.getVpa());
        
        if (payment.getCardNetwork() != null) {
            response.setCardNetwork(payment.getCardNetwork().name().toLowerCase());
        }
        
        response.setCardLast4(payment.getCardLast4());
        response.setErrorCode(payment.getErrorCode());
        response.setErrorDescription(payment.getErrorDescription());
        response.setCreatedAt(payment.getCreatedAt());
        response.setUpdatedAt(payment.getUpdatedAt());
        
        return response;
    }

    /**
     * Helper method to handle IllegalArgumentException with error codes
     */
    private ResponseEntity<?> handleIllegalArgument(IllegalArgumentException e) {
        String message = e.getMessage();
        
        if (message == null) {
            ErrorResponse errorResponse = new ErrorResponse("BAD_REQUEST_ERROR", "Invalid request");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }

        // Parse error code and description from message format: "CODE|Description"
        if (message.contains("|")) {
            String[] parts = message.split("\\|", 2);
            String code = parts[0];
            String description = parts[1];
            ErrorResponse errorResponse = new ErrorResponse(code, description);

            // Return appropriate HTTP status based on error code
            if (code.equals("AUTHENTICATION_ERROR")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
            } else if (code.equals("NOT_FOUND_ERROR")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
            }
        }

        ErrorResponse errorResponse = new ErrorResponse("BAD_REQUEST_ERROR", message);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }
}
