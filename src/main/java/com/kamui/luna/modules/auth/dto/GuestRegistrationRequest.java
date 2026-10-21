package com.kamui.luna.modules.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record GuestRegistrationRequest(@NotBlank @Size(min = 3, max = 50) String username,
                                       @NotBlank @Size(min = 8, max = 72) String password,
                                       @NotBlank @Size(max = 100) String firstName,
                                       @Pattern(regexp = "^[+0-9() -]{7,20}$") String phoneNumber) {}
