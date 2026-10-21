package com.kamui.luna.modules.menu.service;

import com.kamui.luna.modules.menu.dto.DishRequest;
import com.kamui.luna.modules.menu.dto.DishResponse;
import com.kamui.luna.modules.menu.dto.CategoryRequest;
import com.kamui.luna.core.exception.NotFoundException;
import com.kamui.luna.modules.menu.entity.Category;
import com.kamui.luna.modules.menu.entity.Dish;
import com.kamui.luna.modules.menu.repository.CategoryRepository;
import com.kamui.luna.modules.menu.repository.DishRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MenuService {

    private final DishRepository dishRepository;
    private final CategoryRepository categoryRepository;

    public MenuService(DishRepository dishRepository, CategoryRepository categoryRepository) {
        this.dishRepository = dishRepository;
        this.categoryRepository = categoryRepository;
    }

    // Чтение не требует строгой транзакции, оптимизируем через readOnly = true
    @Transactional(readOnly = true)
    public List<DishResponse> getAllAvailableDishes() {
        return dishRepository.findByIsAvailableTrue().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public DishResponse createDish(DishRequest request) {
        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new NotFoundException("Категория с ID " + request.categoryId() + " не найдена"));

        Dish dish = new Dish();
        dish.setName(request.name());
        dish.setCategory(category);
        dish.setDescription(request.description());
        dish.setPrice(request.price());
        dish.setImageUrl(request.imageUrl());

        Dish savedDish = dishRepository.save(dish);
        return mapToResponse(savedDish);
    }

    @Transactional
    public void toggleStopList(Long dishId, boolean isAvailable) {
        Dish dish = dishRepository.findById(dishId)
                .orElseThrow(() -> new NotFoundException("Блюдо не найдено"));
        dish.setAvailable(isAvailable);
        dishRepository.save(dish);
    }

    @Transactional
    public DishResponse updateDish(Long id, DishRequest request) {
        Dish dish = dishRepository.findById(id).orElseThrow(() -> new NotFoundException("Блюдо не найдено"));
        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new NotFoundException("Категория не найдена"));
        dish.setName(request.name().trim()); dish.setCategory(category); dish.setDescription(request.description());
        dish.setPrice(request.price()); dish.setImageUrl(request.imageUrl());
        return mapToResponse(dishRepository.save(dish));
    }

    @Transactional
    public Category createCategory(CategoryRequest request) {
        Category category = new Category();
        category.setName(request.name().trim());
        category.setSortOrder(request.sortOrder());
        return categoryRepository.save(category);
    }

    @Transactional(readOnly = true)
    public List<Category> categories() { return categoryRepository.findAll(); }

    // Вспомогательный метод маппинга Entity -> DTO
    private DishResponse mapToResponse(Dish dish) {
        return new DishResponse(
                dish.getId(),
                dish.getName(),
                dish.getCategory().getName(),
                dish.getDescription(),
                dish.getPrice(),
                dish.isAvailable(),
                dish.getImageUrl()
        );
    }
}
