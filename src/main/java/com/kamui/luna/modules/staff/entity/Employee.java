package com.kamui.luna.modules.staff.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;

@Entity
@Table(name = "employees", schema = "staff")
@Getter @Setter @NoArgsConstructor
public class Employee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Ссылка на ID из модуля auth. Физический Foreign Key не делаем ради изоляции модулей
    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(nullable = false, length = 100)
    private String position;

    @Column(name = "hourly_rate", nullable = false, precision = 19, scale = 4)
    private BigDecimal hourlyRate = BigDecimal.ZERO;

    @Column(name = "sales_percentage", nullable = false, precision = 5, scale = 2)
    private BigDecimal salesPercentage = BigDecimal.ZERO;
}