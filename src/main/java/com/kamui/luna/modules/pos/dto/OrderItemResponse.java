package com.kamui.luna.modules.pos.dto;

import java.math.BigDecimal;

public record OrderItemResponse(Long id, Long dishId, String dishName, int quantity,
                                BigDecimal unitPrice, BigDecimal amount) {}
