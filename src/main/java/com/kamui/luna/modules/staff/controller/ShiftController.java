package com.kamui.luna.modules.staff.controller;

import com.kamui.luna.modules.staff.entity.Shift;
import com.kamui.luna.modules.staff.service.ShiftService;
import com.kamui.luna.modules.auth.service.CurrentUserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/staff/shifts")
public class ShiftController {

    private final ShiftService shiftService;
    private final CurrentUserService currentUser;

    public ShiftController(ShiftService shiftService, CurrentUserService currentUser) {
        this.shiftService = shiftService;
        this.currentUser = currentUser;
    }

    @PostMapping("/clock-in")
    @PreAuthorize("hasAuthority('staff:shift:manage')") // Право на управление своими сменами
    public ResponseEntity<String> clockIn() {
        shiftService.clockIn(currentUser.id());
        return ResponseEntity.ok("Смена успешно открыта. Хорошего рабочего дня!");
    }

    @PostMapping("/clock-out")
    @PreAuthorize("hasAuthority('staff:shift:manage')")
    public ResponseEntity<String> clockOut() {
        Shift closedShift = shiftService.clockOut(currentUser.id());
        return ResponseEntity.ok("Смена закрыта. Заработано бонусов за сегодня: " + closedShift.getEarnedBonus());
    }
}
