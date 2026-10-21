package com.kamui.luna.modules.finance.controller;

import com.kamui.luna.modules.finance.service.FinanceService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.List;
import com.kamui.luna.modules.finance.dto.TransactionResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

@RestController
@RequestMapping("/api/v1/finance")
public class FinanceController {

    private final FinanceService financeService;

    public FinanceController(FinanceService financeService) {
        this.financeService = financeService;
    }

    // DTO для удобного приема данных
    public record PayrollRequest(@NotNull Long employeeId,
                                 @NotNull @DecimalMin("0") BigDecimal baseSalary,
                                 @NotNull @DecimalMin("0") BigDecimal bonus) {}

    /**
     * Эндпоинт для проведения ежемесячной выплаты сотруднику.
     * Защищено правом доступа: только для владельца или гендиректора.
     */
    @PostMapping("/payroll")
    @PreAuthorize("hasAuthority('finance:payout:execute')")
    public ResponseEntity<String> executePayroll(@Valid @RequestBody PayrollRequest request) {
        financeService.payMonthlySalary(request.employeeId(), request.baseSalary(), request.bonus());
        return ResponseEntity.ok("Выплата успешно проведена и зафиксирована в расходах.");
    }

    @GetMapping("/transactions")
    @PreAuthorize("hasAuthority('finance:transaction:view')")
    public List<TransactionResponse> transactions() { return financeService.transactions(); }
}
