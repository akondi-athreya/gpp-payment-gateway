package com.example.gateway.controllers;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.Valid;
import com.example.gateway.dto.CreateOrderRequest;
import com.example.gateway.dto.ErrorResponse;
import com.example.gateway.models.Order;
import com.example.gateway.services.OrderService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;


@RestController
public class OrderController {

    private final OrderService orderService;

    // Constructor Injection (Best Practice)
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/api/v1/orders")
    public ResponseEntity<?> createOrder(
            @RequestHeader("X-Api-Key") String apiKey,
            @RequestHeader("X-Api-Secret") String apiSecret,
            @Valid @RequestBody CreateOrderRequest request) {

        try {
            Order savedOrder = orderService.createOrder(apiKey, apiSecret, request);

            // Build response
            Map<String, Object> response = new HashMap<>();
            response.put("id", savedOrder.getId());
            response.put("merchant_id", savedOrder.getMerchant().getId());
            response.put("amount", savedOrder.getAmount());
            response.put("currency", savedOrder.getCurrency());
            response.put("receipt", savedOrder.getReceipt());
            response.put("notes", request.getNotes());
            response.put("status", savedOrder.getStatus());
            response.put("created_at", savedOrder.getCreatedAt());

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            String[] parts = e.getMessage().split("\\|");
            String errorCode = parts[0];
            String errorDescription = parts[1];
            
            HttpStatus status = errorCode.equals("AUTHENTICATION_ERROR") 
                ? HttpStatus.UNAUTHORIZED 
                : HttpStatus.BAD_REQUEST;
            
            return ResponseEntity.status(status)
                    .body(new ErrorResponse(errorCode, errorDescription));
        }
    }

    @GetMapping("/api/v1/orders/{order_id}")
    public ResponseEntity<?> getOrder(@PathVariable String order_id) {
        System.out.println(order_id);
        try {
            Order savedOrder = orderService.getOrder(order_id);
            System.out.println(savedOrder);
            
            // Build response to avoid circular references
            Map<String, Object> response = new HashMap<>();
            response.put("id", savedOrder.getId());
            response.put("merchant_id", savedOrder.getMerchant().getId());
            response.put("amount", savedOrder.getAmount());
            response.put("currency", savedOrder.getCurrency());
            response.put("receipt", savedOrder.getReceipt());
            response.put("notes", savedOrder.getNotes());
            response.put("status", savedOrder.getStatus());
            response.put("created_at", savedOrder.getCreatedAt());
            
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (IllegalArgumentException e)  {
            String[] parts = e.getMessage().split("\\|");
            if (parts.length < 2) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ErrorResponse("BAD_REQUEST_ERROR", "Invalid error format"));
            }
            
            String errorCode = parts[0];
            String errorDescription = parts[1];
            
            HttpStatus status;
            if (errorCode.equals("AUTHENTICATION_ERROR")) {
                status = HttpStatus.UNAUTHORIZED;
            } else if (errorCode.equals("NOT_FOUND_ERROR")) {
                status = HttpStatus.NOT_FOUND;
            } else {
                status = HttpStatus.BAD_REQUEST;
            }
            
            return ResponseEntity.status(status)
                    .body(new ErrorResponse(errorCode, errorDescription));
        }
    }

    @GetMapping("/api/v1/orders/{order_id}/public")
    public ResponseEntity<?> getOrderPublic(@PathVariable String order_id) {
        try {
            Order savedOrder = orderService.getOrder(order_id);
            
            // Build response to avoid circular references
            Map<String, Object> response = new HashMap<>();
            response.put("id", savedOrder.getId());
            response.put("merchant_id", savedOrder.getMerchant().getId());
            response.put("amount", savedOrder.getAmount());
            response.put("currency", savedOrder.getCurrency());
            response.put("receipt", savedOrder.getReceipt());
            response.put("notes", savedOrder.getNotes());
            response.put("status", savedOrder.getStatus());
            response.put("created_at", savedOrder.getCreatedAt());
            
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (IllegalArgumentException e)  {
            String[] parts = e.getMessage().split("\\|");
            if (parts.length < 2) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ErrorResponse("BAD_REQUEST_ERROR", "Invalid error format"));
            }
            
            String errorCode = parts[0];
            String errorDescription = parts[1];
            
            HttpStatus status;
            if (errorCode.equals("NOT_FOUND_ERROR")) {
                status = HttpStatus.NOT_FOUND;
            } else {
                status = HttpStatus.BAD_REQUEST;
            }
            
            return ResponseEntity.status(status)
                    .body(new ErrorResponse(errorCode, errorDescription));
        }
    }
    
}
