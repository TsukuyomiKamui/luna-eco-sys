package com.kamui.luna.modules.finance.service;

import com.kamui.luna.modules.finance.entity.FinancialTransaction;
import com.kamui.luna.modules.finance.entity.TransactionCategory;
import com.kamui.luna.modules.finance.repository.FinancialTransactionRepository;
import com.kamui.luna.modules.finance.repository.TransactionCategoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;
import com.kamui.luna.modules.finance.dto.TransactionResponse;
import com.kamui.luna.modules.staff.repository.EmployeeRepository;
import com.kamui.luna.core.exception.NotFoundException;

@Service
public class FinanceService {

    private final FinancialTransactionRepository transactionRepository;
    private final TransactionCategoryRepository categoryRepository;
    private final EmployeeRepository employeeRepository;

    public FinanceService(FinancialTransactionRepository transactionRepository,
                          TransactionCategoryRepository categoryRepository, EmployeeRepository employeeRepository) {
        this.transactionRepository = transactionRepository;
        this.categoryRepository = categoryRepository;
        this.employeeRepository = employeeRepository;
    }

    /**
     * Автоматически фиксирует доход ресторана при закрытии чека в зале.
     */
    @Transactional
    public void registerPosIncome(Long orderId, BigDecimal amount) {
        TransactionCategory category = categoryRepository.findByName("Выручка POS (Зал)")
                .orElseThrow(() -> new NotFoundException("Категория доходов 'Выручка POS (Зал)' не найдена в БД"));

        FinancialTransaction transaction = new FinancialTransaction();
        transaction.setCategory(category);
        transaction.setAmount(amount);
        transaction.setDescription("Оплата заказа #" + orderId);

        transactionRepository.save(transaction);
    }

    /**
     * Ручная выплата зарплаты и премии управляющим раз в месяц.
     */
    @Transactional
    public void payMonthlySalary(Long employeeId, BigDecimal baseSalary, BigDecimal earnedBonus) {
        if (!employeeRepository.existsById(employeeId)) throw new NotFoundException("Сотрудник не найден");
        TransactionCategory category = categoryRepository.findByName("Зарплата и премии")
                .orElseThrow(() -> new NotFoundException("Категория расходов 'Зарплата и премии' не найдена в БД"));

        BigDecimal totalPayout = baseSalary.add(earnedBonus);

        FinancialTransaction transaction = new FinancialTransaction();
        transaction.setCategory(category);
        transaction.setAmount(totalPayout);
        transaction.setDescription("Выплата ЗП и премии (Оклад: " + baseSalary + ", Премия: " + earnedBonus + ") сотруднику ID " + employeeId);

        // Расход фиксируется в журнале. Баланс ресторана уменьшается.
        transactionRepository.save(transaction);

        // ПРИМЕЧАНИЕ: Здесь в будущем можно кидать событие SalaryPaidEvent,
        // чтобы модуль staff обнулил накопленные бонусы сотрудника за этот месяц!
    }

    @Transactional(readOnly = true)
    public List<TransactionResponse> transactions() {
        return transactionRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(t -> new TransactionResponse(t.getId(), t.getCategory().getName(), t.getCategory().getType(),
                        t.getAmount(), t.getDescription(), t.getCreatedAt())).toList();
    }
}
