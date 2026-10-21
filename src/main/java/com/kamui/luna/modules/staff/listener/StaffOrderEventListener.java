package com.kamui.luna.modules.staff.listener;

import com.kamui.luna.core.event.OrderPaidEvent;
import com.kamui.luna.modules.staff.service.ShiftService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.stereotype.Component;

@Component
public class StaffOrderEventListener {

    private final ShiftService shiftService;

    public StaffOrderEventListener(ShiftService shiftService) {
        this.shiftService = shiftService;
    }

    /**
     * Перехватываем событие оплаты из POS и начисляем бонус официанту.
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async // Рекомендуется выполнять асинхронно, чтобы не тормозить процесс закрытия чека гостя
    public void handleOrderPaid(OrderPaidEvent event) {
        // event.waiterId() — это ID пользователя в системе
        shiftService.calculateAndAddBonusFromOrder(event.waiterId(), event.totalAmount());
    }
}
