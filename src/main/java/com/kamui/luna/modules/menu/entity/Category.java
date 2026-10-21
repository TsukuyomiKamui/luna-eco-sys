package com.kamui.luna.modules.menu.entity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "categories", schema = "menu")
@Getter @Setter @NoArgsConstructor
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;
}

