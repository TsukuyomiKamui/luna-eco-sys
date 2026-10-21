package com.kamui.luna.modules.finance.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransactionResponse(Long id, String category, String type, BigDecimal amount,
                                  String description, LocalDateTime createdAt) {}
