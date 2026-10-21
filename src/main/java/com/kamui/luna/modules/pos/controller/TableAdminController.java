package com.kamui.luna.modules.pos.controller;

import com.kamui.luna.modules.pos.entity.RestaurantTable;
import com.kamui.luna.modules.pos.repository.RestaurantTableRepository;
import com.kamui.luna.modules.pos.dto.TableRequest;
import com.kamui.luna.core.exception.NotFoundException;
import jakarta.validation.Valid;
import java.util.List;
import com.kamui.luna.modules.pos.repository.OrderRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/pos/tables")
public class TableAdminController {

    private final RestaurantTableRepository tableRepository;
    private final OrderRepository orderRepository;

    public TableAdminController(RestaurantTableRepository tableRepository, OrderRepository orderRepository) {
        this.tableRepository = tableRepository;
        this.orderRepository = orderRepository;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('pos:table:manage')") // Только для менеджеров/админов
    public ResponseEntity<RestaurantTable> createTable(@Valid @RequestBody TableRequest request) {
        RestaurantTable table = new RestaurantTable();
        table.setTableNumber(request.tableNumber());
        table.setCapacity(request.capacity());
        return ResponseEntity.ok(tableRepository.save(table));
    }

    @GetMapping
    public List<RestaurantTable> list() {
        return tableRepository.findAllByOrderByTableNumberAsc();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('pos:table:manage')")
    public ResponseEntity<Void> deleteTable(@PathVariable Long id) {
        RestaurantTable table = tableRepository.findById(id).orElseThrow(() -> new NotFoundException("Стол не найден"));
        if (!"FREE".equals(table.getStatus())) throw new IllegalArgumentException("Нельзя удалить занятый стол");
        if (orderRepository.existsByTableId(id)) throw new IllegalArgumentException("Нельзя удалить стол с историей заказов");
        tableRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
