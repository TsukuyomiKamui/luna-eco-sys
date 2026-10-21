package com.kamui.luna.modules.finance.listener;

import com.kamui.luna.core.event.OrderPaidEvent;
import com.kamui.luna.modules.finance.service.FinanceService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.stereotype.Component;

@Component
public class FinanceOrderEventListener {

    private final FinanceService financeService;

    public FinanceOrderEventListener(FinanceService financeService) {
        this.financeService = financeService;
    }

    /**
     * Ловим событие оплаты из кассы и пополняем виртуальный баланс ресторана.
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async
    public void handleOrderPaid(OrderPaidEvent event) {
        financeService.registerPosIncome(event.orderId(), event.totalAmount());
    }
}
