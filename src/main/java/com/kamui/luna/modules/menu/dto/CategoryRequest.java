package com.kamui.luna.modules.menu.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record CategoryRequest(@NotBlank String name, @Min(0) int sortOrder) {}
