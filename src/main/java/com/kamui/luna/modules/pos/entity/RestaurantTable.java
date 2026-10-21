package com.kamui.luna.modules.pos.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "restaurant_tables", schema = "pos")
@Getter @Setter @NoArgsConstructor
public class RestaurantTable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "table_number", nullable = false, unique = true)
    private Integer tableNumber;

    @Column(nullable = false)
    private Integer capacity; // На сколько персон рассчитан

    @Column(nullable = false, length = 50)
    private String status = "FREE"; // FREE, OCCUPIED, RESERVED
}