package org.scoooting.user.adapters.infrastructure.kafka.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record AwardBonusesDTO(
        @NotNull Long userId,
        @NotNull @Min(0) Integer amount
) {}