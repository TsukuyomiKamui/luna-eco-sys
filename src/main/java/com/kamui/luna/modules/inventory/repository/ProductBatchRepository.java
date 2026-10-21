package com.kamui.luna.modules.inventory.repository;

import com.kamui.luna.modules.inventory.entity.ProductBatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.util.List;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;

@Repository
public interface ProductBatchRepository extends JpaRepository<ProductBatch, Long> {

    // ДОСТАЕТ ПАРТИИ ПО FIFO: Берет только те, где остаток > 0, сортирует по дате годности
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<ProductBatch> findByIngredientIdAndQuantityGreaterThanOrderByExpirationDateAsc(Long ingredientId, BigDecimal minQuantity);
    List<ProductBatch> findAllByOrderByExpirationDateAsc();
}
