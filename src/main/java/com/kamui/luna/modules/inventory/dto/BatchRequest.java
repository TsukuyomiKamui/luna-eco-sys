package com.kamui.luna.modules.inventory.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record BatchRequest(@NotNull Long ingredientId,
                           @NotNull @DecimalMin(value = "0", inclusive = false) BigDecimal quantity,
                           @NotNull @DecimalMin("0") BigDecimal purchasePrice,
                           @NotNull @Future LocalDateTime expirationDate) {}
