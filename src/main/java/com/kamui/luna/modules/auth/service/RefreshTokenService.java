package com.kamui.luna.modules.auth.service;

import com.kamui.luna.modules.auth.entity.RefreshToken;
import com.kamui.luna.modules.auth.repository.RefreshTokenRepository;
import com.kamui.luna.modules.auth.repository.UserRepository;
import com.kamui.luna.modules.auth.entity.User;
import com.kamui.luna.core.exception.BusinessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;

@Service
public class RefreshTokenService {
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    @Value("${app.jwt.refresh-expiration-ms}") private long refreshExpirationMs;

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository, UserRepository userRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.userRepository = userRepository;
    }

    public RefreshToken createRefreshToken(Long userId) {
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(userRepository.findById(userId).orElseThrow());
        refreshToken.setToken(UUID.randomUUID().toString()); // Просто уникальная строка-билет
        refreshToken.setExpiryDate(Instant.now().plusMillis(refreshExpirationMs));

        return refreshTokenRepository.save(refreshToken);
    }

    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.getExpiryDate().isBefore(Instant.now())) {
            refreshTokenRepository.delete(token);
            throw new BusinessException("Refresh token устарел. Пожалуйста, зайдите в систему заново.");
        }
        return token;
    }

    @Transactional
    public void deleteByUserId(Long userId) {
        refreshTokenRepository.deleteByUserId(userId);
    }

    @Transactional
    public RefreshToken getOrCreate(User user) {
        return refreshTokenRepository.findByUserUsername(user.getUsername()).map(token -> {
            if (token.getExpiryDate().isAfter(Instant.now())) return token;
            refreshTokenRepository.delete(token);
            return createRefreshToken(user.getId());
        }).orElseGet(() -> createRefreshToken(user.getId()));
    }
}
