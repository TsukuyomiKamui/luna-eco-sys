package com.kamui.luna.modules.pos.repository;

import com.kamui.luna.modules.pos.entity.RestaurantTable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RestaurantTableRepository extends JpaRepository<RestaurantTable, Long> {
    // Отдать фронтенду список всех столов для отрисовки карты зала
    List<RestaurantTable> findAllByOrderByTableNumberAsc();
}