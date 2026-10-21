package com.kamui.luna.modules.staff.controller;

import com.kamui.luna.modules.auth.service.AccountService;
import com.kamui.luna.modules.staff.dto.EmployeeRegistrationRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/staff")
public class StaffAdminController {
    private final AccountService accounts;
    public StaffAdminController(AccountService accounts) { this.accounts = accounts; }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('staff:employee:manage')")
    public Long create(@Valid @RequestBody EmployeeRegistrationRequest request) {
        return accounts.registerEmployee(request);
    }
}
