package com.example.gateway.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.example.gateway.models.Merchant;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MerchantRepository extends JpaRepository<Merchant, UUID> {
    
    @Query("SELECT m FROM Merchant m WHERE m.apiKey = :apiKey AND m.apiSecret = :apiSecret")
    Optional<Merchant> findByApiKeyAndApiSecret(@Param("apiKey") String apiKey, @Param("apiSecret") String apiSecret);
}
