package com.kamui.luna.modules.pos.repository;

import com.kamui.luna.modules.pos.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    // Метод для проверки, есть ли уже открытый счет за этим столом
    Optional<Order> findByTableIdAndStatus(Long tableId, String status);
    List<Order> findByStatusOrderByCreatedAtDesc(String status);
    List<Order> findAllByOrderByCreatedAtDesc();
    boolean existsByTableId(Long tableId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select o from Order o where o.id = :id")
    Optional<Order> findByIdForUpdate(@Param("id") Long id);
}
