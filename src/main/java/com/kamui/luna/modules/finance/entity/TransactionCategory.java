package com.kamui.luna.modules.finance.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "transaction_categories", schema = "finance")
@Getter @Setter @NoArgsConstructor
public class TransactionCategory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(nullable = false, length = 20)
    private String type; // "INCOME" или "EXPENSE"
}