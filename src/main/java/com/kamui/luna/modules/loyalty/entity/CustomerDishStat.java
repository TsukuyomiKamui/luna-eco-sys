package com.kamui.luna.modules.loyalty.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "customer_dish_stats", schema = "loyalty")
@Getter @Setter @NoArgsConstructor
public class CustomerDishStat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Column(name = "dish_id", nullable = false)
    private Long dishId;

    @Column(name = "order_count", nullable = false)
    private Integer orderCount = 1;

    public void incrementCount() {
        this.orderCount++;
    }
}