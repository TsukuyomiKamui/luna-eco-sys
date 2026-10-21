package com.kamui.luna.modules.inventory.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record RecipeItemRequest(@NotNull Long ingredientId,
                                @NotNull @DecimalMin(value = "0", inclusive = false) BigDecimal amount) {}
