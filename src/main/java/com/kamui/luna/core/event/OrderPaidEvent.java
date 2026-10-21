package com.kamui.luna.core.event;

import java.math.BigDecimal;
import java.util.List;

//событие не содержит JPA-сущности, только примитивы и Id
public record OrderPaidEvent(
        Long orderId,
        Long tableId,
        Long waiterId,
        Long customerId,
        BigDecimal totalAmount,
        List<SoldItem> items
) {}
