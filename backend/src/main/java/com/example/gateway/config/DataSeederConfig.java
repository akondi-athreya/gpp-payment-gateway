package com.example.gateway.config;

import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.UUID;
import com.example.gateway.models.Merchant;
import com.example.gateway.repositories.MerchantRepository;

@Configuration
public class DataSeederConfig {

    @Bean
    public ApplicationRunner seedDatabase(MerchantRepository merchantRepository) {
        return args -> {
            try {
                // Check if test merchant already exists by email
                var existingMerchant = merchantRepository.findAll()
                    .stream()
                    .filter(m -> m.getEmail().equals("test@example.com"))
                    .findFirst();

                if (existingMerchant.isEmpty()) {
                    // Create test merchant
                    Merchant testMerchant = new Merchant();
                    testMerchant.setName("Test Merchant");
                    testMerchant.setEmail("test@example.com");
                    testMerchant.setApiKey("key_test_abc123");
                    testMerchant.setApiSecret("secret_test_xyz789");

                    merchantRepository.save(testMerchant);
                    System.out.println("✓ Test merchant created successfully");
                } else {
                    System.out.println("✓ Test merchant already exists, skipping insertion");
                }
            } catch (Exception e) {
                System.err.println("✗ Error creating test merchant: " + e.getMessage());
                e.printStackTrace();
            }
        };
    }
}
