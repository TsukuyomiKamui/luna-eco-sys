package com.kamui.luna.modules.inventory.repository;

import com.kamui.luna.modules.inventory.entity.Ingredient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IngredientRepository extends JpaRepository<Ingredient, Long> {

    // Поиск ингредиента по уникальному артикулу (SKU)
    Optional<Ingredient> findBySku(String sku);

    // Поиск ингредиента по точному названию
    Optional<Ingredient> findByName(String name);
}