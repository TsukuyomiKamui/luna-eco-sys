package com.kamui.luna.modules.pos.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record TableRequest(@NotNull @Min(1) Integer tableNumber, @NotNull @Min(1) Integer capacity) {}
