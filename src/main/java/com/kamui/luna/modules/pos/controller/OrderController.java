package com.kamui.luna.modules.pos.controller;

import com.kamui.luna.modules.pos.service.OrderService;
import com.kamui.luna.modules.pos.dto.OrderResponse;
import com.kamui.luna.modules.pos.dto.PayOrderRequest;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/pos/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    /**
     * Закрыть чек (принять оплату).
     * Доступно только персоналу с правом 'pos:order:close'.
     */
    @PostMapping("/{orderId}/pay")
    @PreAuthorize("hasAuthority('pos:order:close')")
    public ResponseEntity<OrderResponse> payOrder(@PathVariable Long orderId,
                                                   @RequestBody(required = false) PayOrderRequest request) {
        return ResponseEntity.ok(orderService.payOrder(orderId, request == null ? null : request.customerId()));
    }

    @GetMapping("/{orderId}")
    @PreAuthorize("hasAuthority('pos:order:view')")
    public OrderResponse get(@PathVariable Long orderId) {
        return orderService.get(orderId);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('pos:order:view')")
    public List<OrderResponse> list(@RequestParam(required = false) String status) {
        return orderService.list(status);
    }

    @PostMapping("/{orderId}/cancel")
    @PreAuthorize("hasAuthority('pos:order:close')")
    public OrderResponse cancel(@PathVariable Long orderId) { return orderService.cancel(orderId); }
}
