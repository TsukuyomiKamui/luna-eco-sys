package com.kamui.luna.modules.inventory.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record RecipeRequest(@NotNull Long dishId, @NotEmpty List<@Valid RecipeItemRequest> items) {}
