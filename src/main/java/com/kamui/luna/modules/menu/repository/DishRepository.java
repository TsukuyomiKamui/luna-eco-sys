package com.kamui.luna.modules.menu.repository;

import com.kamui.luna.modules.menu.entity.Dish;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface DishRepository extends JpaRepository<Dish, Long> {
    // Получить все блюда в определенной категории
    List<Dish> findByCategoryId(Long categoryId);

    // Получить только те блюда, которые сейчас есть на кухне (не в стоп-листе)
    List<Dish> findByIsAvailableTrue();
}