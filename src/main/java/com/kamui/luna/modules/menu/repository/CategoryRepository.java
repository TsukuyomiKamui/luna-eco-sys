package com.kamui.luna.modules.menu.repository;

import com.kamui.luna.modules.menu.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
}