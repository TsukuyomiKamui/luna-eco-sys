package com.kamui.luna.modules.menu.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record DishRequest(
        @NotBlank(message = "Название блюда не может быть пустым")
        String name,

        @NotNull(message = "ID категории обязательно")
        Long categoryId,

        String description,

        @NotNull(message = "Цена обязательна")
        @DecimalMin(value = "0.0", inclusive = false, message = "Цена должна быть больше нуля")
        BigDecimal price,

        String imageUrl
) {}