package com.kamui.luna.modules.loyalty.listener;

import com.kamui.luna.core.event.OrderPaidEvent;
import com.kamui.luna.modules.loyalty.service.LoyaltyService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.stereotype.Component;

@Component
public class LoyaltyOrderEventListener {

    private final LoyaltyService loyaltyService;

    public LoyaltyOrderEventListener(LoyaltyService loyaltyService) {
        this.loyaltyService = loyaltyService;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async
    public void handleOrderPaid(OrderPaidEvent event) {
        // Если customerId не null (гость привязал карту лояльности к чеку)
        if (event.customerId() != null) {
            loyaltyService.processGuestOrder(event.customerId(), event.totalAmount(), event.items());
        }
    }
}
