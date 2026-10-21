package com.kamui.luna.modules.pos.controller;

import com.kamui.luna.modules.pos.dto.AddItemRequest;
import com.kamui.luna.modules.pos.service.OrderService;
import com.kamui.luna.modules.pos.dto.OrderResponse;
import com.kamui.luna.modules.auth.service.CurrentUserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/staff/pos/tables")
public class WaiterController {

    private final OrderService orderService;
    private final CurrentUserService currentUser;

    public WaiterController(OrderService orderService, CurrentUserService currentUser) {
        this.orderService = orderService;
        this.currentUser = currentUser;
    }

    /**
     * Добавить блюда на конкретный столик.
     * ID официанта берется не из тела запроса (чтобы нельзя было подделать),
     * а достается из контекста безопасности (из JWT токена).
     */
    @PostMapping("/{tableId}/items")
    @PreAuthorize("hasAuthority('pos:order:create')")
    public ResponseEntity<OrderResponse> addItems(@PathVariable Long tableId,
                                                   @RequestBody List<@Valid AddItemRequest> items) {
        return ResponseEntity.ok(orderService.addItemsToTable(tableId, currentUser.id(), items));
    }
}
