package com.kamui.luna.modules.inventory.dto;

import jakarta.validation.constraints.NotBlank;

public record IngredientRequest(@NotBlank String name, @NotBlank String sku, @NotBlank String baseUnit) {}
