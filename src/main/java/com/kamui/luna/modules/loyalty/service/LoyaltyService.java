package com.kamui.luna.modules.loyalty.service;

import com.kamui.luna.modules.loyalty.entity.Customer;
import com.kamui.luna.modules.loyalty.entity.CustomerDishStat;
import com.kamui.luna.modules.loyalty.repository.CustomerDishStatRepository;
import com.kamui.luna.modules.loyalty.repository.CustomerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import com.kamui.luna.core.event.SoldItem;
import com.kamui.luna.core.exception.NotFoundException;

@Service
public class LoyaltyService {

    private final CustomerRepository customerRepository;
    private final CustomerDishStatRepository statRepository;

    public LoyaltyService(CustomerRepository customerRepository, CustomerDishStatRepository statRepository) {
        this.customerRepository = customerRepository;
        this.statRepository = statRepository;
    }

    /**
     * Вызывается асинхронно при оплате заказа.
     * Начисляет бонусы и обновляет любимое блюдо гостя.
     */
    @Transactional
    public void processGuestOrder(Long customerId, BigDecimal totalAmount, List<SoldItem> items) {
        if (customerId == null) return; // Если гость не авторизован (просто зашел с улицы), ничего не делаем

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new NotFoundException("Профиль гостя не найден"));

        // 1. Начисление кэшбека
        BigDecimal cashbackPercent = getCashbackPercent(customer.getLoyaltyLevel());
        BigDecimal bonusesEarned = totalAmount.multiply(cashbackPercent)
                .divide(BigDecimal.valueOf(100), RoundingMode.HALF_UP);

        customer.addBonuses(bonusesEarned);

        // 2. Обновление статистики по съеденным блюдам
        for (SoldItem item : items) {
            CustomerDishStat stat = statRepository.findByCustomerIdAndDishId(customer.getId(), item.dishId())
                    .orElseGet(() -> {
                        CustomerDishStat newStat = new CustomerDishStat();
                        newStat.setCustomer(customer);
                        newStat.setDishId(item.dishId());
                        newStat.setOrderCount(0); // Инициализируем нулем, ниже сделаем +1
                        return newStat;
                    });
            for (int i = 0; i < item.quantity(); i++) stat.incrementCount();
            statRepository.save(stat);
        }

        // 3. Вычисляем новое "Любимое блюдо" (То, которое заказывали чаще всего)
        List<CustomerDishStat> allStats = statRepository.findByCustomerIdOrderByOrderCountDesc(customer.getId());
        if (!allStats.isEmpty()) {
            customer.setFavoriteDishId(allStats.get(0).getDishId());
        }

        customerRepository.save(customer);
    }

    private BigDecimal getCashbackPercent(String level) {
        return switch (level.toUpperCase()) {
            case "SILVER" -> BigDecimal.valueOf(10.0);
            case "GOLD" -> BigDecimal.valueOf(15.0);
            default -> BigDecimal.valueOf(5.0); // BRONZE по умолчанию
        };
    }
}
