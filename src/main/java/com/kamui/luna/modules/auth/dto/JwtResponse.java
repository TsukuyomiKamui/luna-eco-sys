package com.kamui.luna.modules.auth.dto;

import java.util.Set;

public record JwtResponse(
        String token,
        String refreshToken,
        Set<String> permissions
) {
}
