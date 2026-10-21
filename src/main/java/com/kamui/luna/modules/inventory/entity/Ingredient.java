package com.kamui.luna.modules.inventory.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "ingredients", schema = "inventory")
@Getter @Setter @NoArgsConstructor
public class Ingredient {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 150)
    private String name;

    @Column(nullable = false, unique = true, length = 50)
    private String sku;

    @Column(name = "base_unit", nullable = false, length = 20)
    private String baseUnit;
}