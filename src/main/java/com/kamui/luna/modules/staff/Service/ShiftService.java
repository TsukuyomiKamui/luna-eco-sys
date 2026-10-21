package com.kamui.luna.modules.staff.service;

import com.kamui.luna.modules.staff.entity.Employee;
import com.kamui.luna.modules.staff.entity.Shift;
import com.kamui.luna.modules.staff.repository.EmployeeRepository;
import com.kamui.luna.modules.staff.repository.ShiftRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import com.kamui.luna.core.exception.NotFoundException;
import com.kamui.luna.core.exception.BusinessException;

@Service
public class ShiftService {

    private final EmployeeRepository employeeRepository;
    private final ShiftRepository shiftRepository;

    public ShiftService(EmployeeRepository employeeRepository, ShiftRepository shiftRepository) {
        this.employeeRepository = employeeRepository;
        this.shiftRepository = shiftRepository;
    }

    /**
     * Официант пришел на работу и "прокатал" карточку (Clock-In)
     */
    @Transactional
    public Shift clockIn(Long userId) {
        Employee employee = employeeRepository.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException("Профиль сотрудника не найден"));

        // Проверяем, нет ли уже открытой смены, чтобы не плодить дубликаты
        shiftRepository.findByEmployeeIdAndStatus(employee.getId(), "OPEN")
                .ifPresent(shift -> {
                    throw new BusinessException("Смена уже открыта. Сначала закройте предыдущую.");
                });

        Shift newShift = new Shift();
        newShift.setEmployee(employee);

        return shiftRepository.save(newShift);
    }

    /**
     * Официант уходит домой (Clock-Out)
     */
    @Transactional
    public Shift clockOut(Long userId) {
        Employee employee = employeeRepository.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException("Профиль сотрудника не найден"));

        Shift activeShift = shiftRepository.findByEmployeeIdAndStatus(employee.getId(), "OPEN")
                .orElseThrow(() -> new NotFoundException("Активная смена не найдена"));

        activeShift.setEndTime(LocalDateTime.now());
        activeShift.setStatus("CLOSED");

        return shiftRepository.save(activeShift);
    }

    /**
     * Вызывается автоматически шиной событий при оплате заказа.
     * Рассчитывает бонус от суммы чека и добавляет его в копилку за текущую смену.
     */
    @Transactional
    public void calculateAndAddBonusFromOrder(Long userId, BigDecimal orderTotalAmount) {
        Employee employee = employeeRepository.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException("Сотрудник не найден"));

        // Ищем активную смену. Если официант забыл открыть смену, бонус просто сгорает
        // (или можно автоматически открыть смену, зависит от политики ресторана)
        shiftRepository.findByEmployeeIdAndStatus(employee.getId(), "OPEN").ifPresent(shift -> {

            // Если у официанта ставка продаж больше 0%
            if (employee.getSalesPercentage().compareTo(BigDecimal.ZERO) > 0) {
                // Расчет процента: (Сумма * Процент) / 100
                BigDecimal bonusAmount = orderTotalAmount
                        .multiply(employee.getSalesPercentage())
                        .divide(BigDecimal.valueOf(100), RoundingMode.HALF_UP);

                shift.addBonus(bonusAmount);
                shiftRepository.save(shift);
            }
        });
    }
}
