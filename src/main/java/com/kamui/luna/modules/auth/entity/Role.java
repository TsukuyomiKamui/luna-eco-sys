package com.kamui.luna.modules.auth.entity;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

import lombok.*;

@Entity
@Table(name = "roles", schema = "auth")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String name;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "role_permission", schema = "auth",
            joinColumns = @JoinColumn(name = "role_id"),
            inverseJoinColumns = @JoinColumn(name = "permission_id")
    )
    private Set<Permisson> permissions = new HashSet<>();
}
