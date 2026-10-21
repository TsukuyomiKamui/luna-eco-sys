package com.kamui.luna.modules.loyalty.repository;

import com.kamui.luna.modules.loyalty.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    Optional<Customer> findByUserId(Long userId);
}