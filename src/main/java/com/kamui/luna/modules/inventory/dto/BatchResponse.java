package com.kamui.luna.modules.inventory.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record BatchResponse(Long id, Long ingredientId, String ingredientName, BigDecimal quantity,
                            BigDecimal purchasePrice, LocalDateTime expirationDate, LocalDateTime receivedAt) {}
