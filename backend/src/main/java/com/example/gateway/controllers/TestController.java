package com.example.gateway.controllers;

import com.example.gateway.repositories.MerchantRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
public class TestController {

    private final MerchantRepository merchantRepository;

    public TestController(MerchantRepository merchantRepository) {
        this.merchantRepository = merchantRepository;
    }

    /**
     * GET /api/v1/test/merchant - Get test merchant details (No authentication required)
     * Used for automated evaluation to verify seeded test merchant exists
     */
    @GetMapping("/api/v1/test/merchant")
    public ResponseEntity<?> getTestMerchant() {
        try {
            // Get the test merchant by email
            var merchants = merchantRepository.findAll();
            var merchant = merchants.stream()
                .filter(m -> m.getEmail().equals("test@example.com"))
                .findFirst();

            if (merchant.isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error_code", "NOT_FOUND_ERROR");
                errorResponse.put("error_description", "Test merchant not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            }

            var testMerchant = merchant.get();

            // Build response - only include required fields for evaluation
            Map<String, Object> response = new HashMap<>();
            response.put("id", testMerchant.getId().toString());
            response.put("email", testMerchant.getEmail());
            response.put("api_key", testMerchant.getApiKey());
            response.put("seeded", true);

            return ResponseEntity.status(HttpStatus.OK).body(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error_code", "BAD_REQUEST_ERROR");
            errorResponse.put("error_description", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }
}
