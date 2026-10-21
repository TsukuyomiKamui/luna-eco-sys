package com.kamui.luna.modules.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record TokenRefreshRequest(
        @NotBlank(message = "Refresh token обязателен")
        String refreshToken
) {
}
