package com.kamui.luna.modules.loyalty.repository;

import com.kamui.luna.modules.loyalty.entity.CustomerDishStat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface CustomerDishStatRepository extends JpaRepository<CustomerDishStat, Long> {
    Optional<CustomerDishStat> findByCustomerIdAndDishId(Long customerId, Long dishId);

    // Получить всю статистику гостя, отсортированную по убыванию (чтобы найти самое популярное блюдо)
    List<CustomerDishStat> findByCustomerIdOrderByOrderCountDesc(Long customerId);
}