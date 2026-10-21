package com.kamui.luna.modules.auth.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "permissions", schema = "auth")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Permisson {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(length = 255)
    private  String description;
}
