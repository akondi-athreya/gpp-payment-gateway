package com.example.gateway.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.example.gateway.models.Payment;
import java.util.List;
import java.util.UUID;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, String> {
    List<Payment> findByMerchantId(UUID merchantId);
}
