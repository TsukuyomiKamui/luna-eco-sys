package com.kamui.luna.modules.pos.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record AddItemRequest(
        @NotNull(message = "ID блюда обязательно")
        Long dishId,

        @NotNull(message = "Количество обязательно")
        @Min(value = 1, message = "Количество должно быть минимум 1")
        Integer quantity
) {}
