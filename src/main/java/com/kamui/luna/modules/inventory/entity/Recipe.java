package com.kamui.luna.modules.inventory.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "recipes", schema = "inventory")
@Getter @Setter @NoArgsConstructor
public class Recipe {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "dish_id", nullable = false, unique = true)
    private Long dishId;

    @OneToMany(mappedBy = "recipe", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RecipeItem> items = new ArrayList<>();
}