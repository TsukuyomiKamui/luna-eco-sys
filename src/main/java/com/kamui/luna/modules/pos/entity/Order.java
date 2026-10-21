package com.kamui.luna.modules.pos.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders", schema = "pos")
@Getter @Setter @NoArgsConstructor
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "table_id", nullable = false)
    private Long tableId;

    @Column(name = "waiter_id", nullable = false)
    private Long waiterId;

    @Column(name = "customer_id")
    private Long customerId;

    @Column(nullable = false, length = 50)
    private String status = "OPEN";

    @Column(name = "total_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    public void addItem(OrderItem item) {
        items.add(item);
        item.setOrder(this);
        // Пересчет суммы чека
        this.totalAmount = this.totalAmount.add(item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
    }
}