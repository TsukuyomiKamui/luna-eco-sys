package com.kamui.luna.modules.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
    @NotBlank(message = "Имя пользователя не может быть пустым")
    String username,

    @NotBlank(message = "Пароль не может быть пустым")
    String password

    ){}
