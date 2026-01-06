package com.example.gateway.services;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.Random;

import org.springframework.stereotype.Service;
import com.example.gateway.dto.CreateOrderRequest;
import com.example.gateway.models.Merchant;
import com.example.gateway.models.Order;
import com.example.gateway.repositories.MerchantRepository;
import com.example.gateway.repositories.OrderRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class OrderService {

    private final MerchantRepository merchantRepository;
    private final OrderRepository orderRepository;
    private final ObjectMapper objectMapper;

    // Constructor Injection (Best Practice)
    public OrderService(MerchantRepository merchantRepository, OrderRepository orderRepository,
            ObjectMapper objectMapper) {
        this.merchantRepository = merchantRepository;
        this.orderRepository = orderRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * Validate and create an order
     */
    public Order createOrder(String apiKey, String apiSecret, CreateOrderRequest request) throws IllegalArgumentException {
        
        // Validate merchant credentials
        System.out.println("DEBUG: Looking up merchant with apiKey=" + apiKey + ", apiSecret=" + apiSecret);
        Optional<Merchant> merchant = merchantRepository.findByApiKeyAndApiSecret(apiKey, apiSecret);
        System.out.println("DEBUG: Merchant found? " + merchant.isPresent());
        
        if (merchant.isEmpty()) {
            System.out.println("DEBUG: Authentication failed");
            throw new IllegalArgumentException("AUTHENTICATION_ERROR|Invalid API credentials");
        }

        Merchant merchantEntity = merchant.get();
        System.out.println("DEBUG: Merchant authenticated: " + merchantEntity.getEmail());

        // Validate amount (after authentication)
        if (request.getAmount() == null || request.getAmount() < 100) {
            System.out.println("DEBUG: Amount validation failed: " + request.getAmount());
            throw new IllegalArgumentException("BAD_REQUEST_ERROR|amount must be at least 100");
        }

        // Create order
        Order order = new Order();
        order.setId(generateOrderId());
        order.setMerchant(merchantEntity);
        order.setAmount(request.getAmount());
        order.setCurrency(request.getCurrency() != null ? request.getCurrency() : "INR");
        order.setReceipt(request.getReceipt());
        
        // Convert notes map to JSON string if present
        if (request.getNotes() != null) {
            try {
                order.setNotes(objectMapper.writeValueAsString(request.getNotes()));
            } catch (Exception e) {
                order.setNotes(null);
            }
        }
        
        order.setStatus("created");
        OffsetDateTime now = OffsetDateTime.now();
        order.setCreatedAt(now);
        order.setUpdatedAt(now);

        // Save order
        return orderRepository.save(order);
    }

    /**
     * Generate a unique order ID with format "order_" followed by 16 alphanumeric characters
     */
    private String generateOrderId() {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        Random random = new Random();
        StringBuilder sb = new StringBuilder("order_");
        
        for (int i = 0; i < 16; i++) {
            sb.append(characters.charAt(random.nextInt(characters.length())));
        }
        
        return sb.toString();
    }

    public Order getOrder(String order_id) {
        return orderRepository.findById(order_id)
                .orElseThrow(() -> new IllegalArgumentException("NOT_FOUND_ERROR|Order not found"));
    }
}
