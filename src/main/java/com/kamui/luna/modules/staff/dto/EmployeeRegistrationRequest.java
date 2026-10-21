package com.kamui.luna.modules.staff.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record EmployeeRegistrationRequest(@NotBlank @Size(min = 3, max = 50) String username,
                                          @NotBlank @Size(min = 8, max = 72) String password,
                                          @NotBlank String firstName, @NotBlank String lastName,
                                          @NotBlank String position, @NotBlank String role,
                                          @NotNull @DecimalMin("0") BigDecimal hourlyRate,
                                          @NotNull @DecimalMin("0") BigDecimal salesPercentage) {}
