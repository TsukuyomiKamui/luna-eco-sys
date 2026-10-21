package com.kamui.luna.modules.loyalty.repository;

import com.kamui.luna.modules.loyalty.entity.Promotion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PromotionRepository extends JpaRepository<Promotion, Long> {

    /**
     * Ищет активные акции для конкретного блюда.
     * Проверяет сразу два условия: флаг активности и попадание текущего времени в период действия акции.
     */
    @Query("SELECT p FROM Promotion p WHERE p.dishId = :dishId AND p.isActive = true AND :now BETWEEN p.startDate AND p.endDate")
    List<Promotion> findActivePromotionsForDish(@Param("dishId") Long dishId, @Param("now") LocalDateTime now);

    /**
     * Достает вообще все действующие акции в ресторане на данный момент.
     * Пригодится для вывода баннеров "Акции дня" на главной странице гостевого приложения.
     */
    @Query("SELECT p FROM Promotion p WHERE p.isActive = true AND :now BETWEEN p.startDate AND p.endDate")
    List<Promotion> findAllActivePromotions(@Param("now") LocalDateTime now);
}