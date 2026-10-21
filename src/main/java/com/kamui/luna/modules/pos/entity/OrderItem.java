package com.kamui.luna.modules.pos.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;

@Entity
@Table(name = "order_items", schema = "pos")
@Getter @Setter @NoArgsConstructor
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(name = "dish_id", nullable = false)
    private Long dishId;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal price;
}