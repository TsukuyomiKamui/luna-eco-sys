package com.kamui.luna.modules.auth.repository;

import com.kamui.luna.modules.auth.entity.RefreshToken;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

    Optional<RefreshToken> findByUserUsername(String username);

    @Modifying
    @Query("DELETE FROM RefreshToken  r WHERE r.user.id = :userId")
    void deleteByUserId(@Param("userId") Long userId);
}
