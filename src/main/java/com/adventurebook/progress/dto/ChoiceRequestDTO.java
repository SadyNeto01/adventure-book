package com.adventurebook.progress.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record ChoiceRequestDTO(@NotNull @PositiveOrZero Integer option) {
}
