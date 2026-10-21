package com.kamui.luna.modules.staff.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "shifts", schema = "staff")
@Getter @Setter @NoArgsConstructor
public class Shift {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(name = "start_time", nullable = false, updatable = false)
    private LocalDateTime startTime = LocalDateTime.now();

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(nullable = false, length = 50)
    private String status = "OPEN";

    @Column(name = "earned_bonus", nullable = false, precision = 19, scale = 4)
    private BigDecimal earnedBonus = BigDecimal.ZERO;

    // Вспомогательный метод для добавления бонуса к текущей смене
    public void addBonus(BigDecimal amount) {
        this.earnedBonus = this.earnedBonus.add(amount);
    }
}