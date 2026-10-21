package com.kamui.luna.modules.auth.entity;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;
import lombok.*;

@Entity
@Table(name = "users", schema = "auth")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(name = "password_hash", nullable = false, length = 100)
    private String passwordHash;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles", schema = "auth",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private  Set<Role> roles = new HashSet<>();
}
