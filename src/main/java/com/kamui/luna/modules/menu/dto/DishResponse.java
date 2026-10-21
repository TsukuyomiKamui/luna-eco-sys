package com.kamui.luna.modules.menu.dto;

import java.math.BigDecimal;

public record DishResponse(
        Long id,
        String name,
        String categoryName,
        String description,
        BigDecimal price,
        boolean isAvailable,
        String imageUrl
) {}