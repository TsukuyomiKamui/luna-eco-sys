package com.kamui.luna.modules.inventory.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "product_batches", schema = "inventory")
@Getter @Setter @NoArgsConstructor
public class ProductBatch {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ingredient_id", nullable = false)
    private Ingredient ingredient;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal quantity;

    @Column(name = "purchase_price", nullable = false, precision = 19, scale = 4)
    private BigDecimal purchasePrice;

    @Column(name = "expiration_date", nullable = false)
    private LocalDateTime expirationDate;

    @Column(name = "received_at", nullable = false, updatable = false)
    private LocalDateTime receivedAt = LocalDateTime.now();
}