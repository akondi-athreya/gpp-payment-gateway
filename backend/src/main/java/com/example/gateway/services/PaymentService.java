package com.example.gateway.services;

import com.example.gateway.models.CardNetwork;
import com.example.gateway.models.PaymentMethod;
import com.example.gateway.models.Merchant;
import com.example.gateway.models.Order;
import com.example.gateway.models.Payment;
import com.example.gateway.repositories.MerchantRepository;
import com.example.gateway.repositories.OrderRepository;
import com.example.gateway.repositories.PaymentRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.Random;

@Service
public class PaymentService {

    private final MerchantRepository merchantRepository;
    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final ValidationService validationService;

    @Value("${TEST_MODE:false}")
    private boolean testMode;

    @Value("${TEST_PAYMENT_SUCCESS:true}")
    private boolean testPaymentSuccess;

    @Value("${TEST_PROCESSING_DELAY:1000}")
    private long testProcessingDelay;

    @Value("${UPI_SUCCESS_RATE:0.90}")
    private double upiSuccessRate;

    @Value("${CARD_SUCCESS_RATE:0.95}")
    private double cardSuccessRate;

    @Value("${PROCESSING_DELAY_MIN:5000}")
    private long processingDelayMin;

    @Value("${PROCESSING_DELAY_MAX:10000}")
    private long processingDelayMax;

    public PaymentService(
            MerchantRepository merchantRepository,
            OrderRepository orderRepository,
            PaymentRepository paymentRepository,
            ValidationService validationService) {
        this.merchantRepository = merchantRepository;
        this.orderRepository = orderRepository;
        this.paymentRepository = paymentRepository;
        this.validationService = validationService;
    }

    /**
     * Create and process a payment
     * Flow:
     * 1. Authenticate merchant
     * 2. Verify order exists and belongs to merchant
     * 3. Validate payment method specific fields
     * 4. Create payment with "processing" status immediately
     * 5. Save to database
     * 6. Process asynchronously (5-10 second delay)
     * 7. Update status based on random success/failure
     */
    public Payment createAndProcessPayment(
            String apiKey,
            String apiSecret,
            String orderId,
            String method,
            String vpa,
            String cardNumber,
            String cardCvv,
            String cardHolderName,
            Integer expiryMonth,
            Integer expiryYear) {

        // 1. Authenticate merchant
        Optional<Merchant> merchantOpt = merchantRepository.findByApiKeyAndApiSecret(apiKey, apiSecret);
        if (merchantOpt.isEmpty()) {
            throw new IllegalArgumentException("AUTHENTICATION_ERROR|Invalid API credentials");
        }
        Merchant merchant = merchantOpt.get();

        // 2. Verify order exists and belongs to merchant
        Optional<Order> orderOpt = orderRepository.findById(orderId);
        if (orderOpt.isEmpty()) {
            throw new IllegalArgumentException("NOT_FOUND_ERROR|Order not found");
        }

        Order order = orderOpt.get();
        if (!order.getMerchant().getId().equals(merchant.getId())) {
            throw new IllegalArgumentException("BAD_REQUEST_ERROR|Order does not belong to this merchant");
        }

        // 3. Validate payment method and create payment object
        PaymentMethod paymentMethod;
        Payment payment = new Payment();
        payment.setId(generatePaymentId());
        payment.setOrder(order);
        payment.setMerchant(merchant);
        payment.setAmount(order.getAmount());
        payment.setCurrency(order.getCurrency());
        OffsetDateTime now = OffsetDateTime.now();
        payment.setCreatedAt(now);
        payment.setUpdatedAt(now);

        if ("upi".equalsIgnoreCase(method)) {
            // Validate UPI
            if (vpa == null || vpa.trim().isEmpty()) {
                throw new IllegalArgumentException("BAD_REQUEST_ERROR|VPA is required for UPI payments");
            }

            if (!validationService.validateVPA(vpa)) {
                throw new IllegalArgumentException("INVALID_VPA|Invalid VPA format");
            }

            paymentMethod = PaymentMethod.UPI;
            payment.setMethod(paymentMethod);
            payment.setVpa(vpa);

        } else if ("card".equalsIgnoreCase(method)) {
            // Validate Card
            if (cardNumber == null || cardNumber.trim().isEmpty()) {
                throw new IllegalArgumentException("BAD_REQUEST_ERROR|Card number is required");
            }

            if (!validationService.validateCardNumber(cardNumber)) {
                throw new IllegalArgumentException("INVALID_CARD|Invalid card number");
            }

            if (expiryMonth == null || expiryYear == null) {
                throw new IllegalArgumentException("BAD_REQUEST_ERROR|Expiry month and year are required");
            }

            if (!validationService.validateExpiryInput(expiryMonth.toString(), expiryYear.toString())) {
                throw new IllegalArgumentException("EXPIRED_CARD|Card has expired or invalid expiry date");
            }

            if (cardCvv == null || cardCvv.trim().isEmpty() || cardCvv.length() < 3) {
                throw new IllegalArgumentException("BAD_REQUEST_ERROR|CVV is required");
            }

            if (cardHolderName == null || cardHolderName.trim().isEmpty()) {
                throw new IllegalArgumentException("BAD_REQUEST_ERROR|Cardholder name is required");
            }

            paymentMethod = PaymentMethod.CARD;
            payment.setMethod(paymentMethod);

            // Detect card network
            CardNetwork network = validationService.getCardNetwork(cardNumber);
            payment.setCardNetwork(network);

            // Store only last 4 digits (NEVER store full card number or CVV)
            String last4 = validationService.getCardLast4(cardNumber);
            payment.setCardLast4(last4);

        } else {
            throw new IllegalArgumentException("BAD_REQUEST_ERROR|Invalid payment method. Use 'upi' or 'card'");
        }

        // 4. Create payment with "processing" status immediately (NOT "created")
        payment.setStatus("processing");

        // 5. Save to database
        Payment savedPayment = paymentRepository.save(payment);

        // 6. Process asynchronously in a separate thread
        new Thread(() -> processPaymentAsync(savedPayment, paymentMethod)).start();

        return savedPayment;
    }

    /**
     * Process payment asynchronously
     * Simulates bank processing with delay, then updates status
     */
    private void processPaymentAsync(Payment payment, PaymentMethod method) {
        try {
            // Apply processing delay (simulates bank processing)
            applyProcessingDelay(method);

            // Determine success/failure
            boolean isSuccessful = determinePaymentSuccess(method);

            // Update payment status
            updatePaymentStatus(payment, isSuccessful);

        } catch (Exception e) {
            // Log error but don't throw - async operation
            System.err.println("Error processing payment: " + e.getMessage());
        }
    }

    /**
     * Apply processing delay
     * In test mode: uses TEST_PROCESSING_DELAY
     * In production: uses random 5-10 seconds
     */
    private void applyProcessingDelay(PaymentMethod method) throws InterruptedException {
        long delay;

        if (testMode) {
            // Test mode: use configured delay (default 1000ms = 1 second)
            delay = testProcessingDelay;
        } else {
            // Production mode: random 5-10 seconds
            Random random = new Random();
            delay = processingDelayMin + random.nextLong(processingDelayMax - processingDelayMin);
        }

        Thread.sleep(delay);
    }

    /**
     * Determine if payment succeeds or fails
     * In test mode: uses TEST_PAYMENT_SUCCESS flag
     * In production: random based on payment method
     *   - UPI: 90% success rate
     *   - Card: 95% success rate
     */
    private boolean determinePaymentSuccess(PaymentMethod method) {
        if (testMode) {
            // Test mode: return configured value
            return testPaymentSuccess;
        }

        // Production mode: random success based on method
        Random random = new Random();
        double randomValue = random.nextDouble();

        if (method == PaymentMethod.UPI) {
            return randomValue < upiSuccessRate;
        } else { // CARD
            return randomValue < cardSuccessRate;
        }
    }

    /**
     * Update payment status after processing
     */
    private void updatePaymentStatus(Payment payment, boolean isSuccessful) {
        payment.setUpdatedAt(OffsetDateTime.now());

        if (isSuccessful) {
            payment.setStatus("success");
        } else {
            payment.setStatus("failed");
            // Set error details for failed payment
            payment.setErrorCode("PAYMENT_FAILED");
            payment.setErrorDescription("Payment processing failed. Please try again.");
        }

        paymentRepository.save(payment);
    }

    /**
     * Get all payments for a merchant (authenticated)
     */
    public java.util.List<Payment> getPaymentsByMerchant(String apiKey, String apiSecret) {
        Optional<Merchant> merchantOpt = merchantRepository.findByApiKeyAndApiSecret(apiKey, apiSecret);
        if (merchantOpt.isEmpty()) {
            throw new IllegalArgumentException("AUTHENTICATION_ERROR|Invalid API credentials");
        }
        Merchant merchant = merchantOpt.get();
        return paymentRepository.findByMerchantId(merchant.getId());
    }

    /**
     * Get payment by ID
     */
    public Payment getPayment(String paymentId) {
        return paymentRepository.findById(paymentId)
                .orElseThrow(() -> new IllegalArgumentException("NOT_FOUND_ERROR|Payment not found"));
    }

    /**
     * Get payment by ID (public endpoint - no merchant check)
     * Used by checkout page to poll status
     */
    public Payment getPaymentPublic(String paymentId) {
        return getPayment(paymentId);
    }

    /**
     * Generate unique payment ID with format: pay_ + 16 alphanumeric characters
     */
    private String generatePaymentId() {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        Random random = new Random();
        StringBuilder sb = new StringBuilder("pay_");

        for (int i = 0; i < 16; i++) {
            sb.append(characters.charAt(random.nextInt(characters.length())));
        }

        return sb.toString();
    }
}
