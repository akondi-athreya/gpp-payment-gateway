package com.example.gateway.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.example.gateway.models.Order;

@Repository
public interface OrderRepository extends JpaRepository<Order, String> {
}
