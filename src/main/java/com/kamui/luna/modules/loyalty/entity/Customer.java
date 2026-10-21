package com.kamui.luna.modules.loyalty.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;

@Entity
@Table(name = "customers", schema = "loyalty")
@Getter @Setter @NoArgsConstructor
public class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "phone_number", length = 20, unique = true)
    private String phoneNumber;

    @Column(name = "bonus_balance", nullable = false, precision = 19, scale = 4)
    private BigDecimal bonusBalance = BigDecimal.ZERO;

    @Column(name = "loyalty_level", nullable = false, length = 20)
    private String loyaltyLevel = "BRONZE";

    @Column(name = "favorite_dish_id")
    private Long favoriteDishId;

    public void addBonuses(BigDecimal amount) {
        this.bonusBalance = this.bonusBalance.add(amount);
    }
}