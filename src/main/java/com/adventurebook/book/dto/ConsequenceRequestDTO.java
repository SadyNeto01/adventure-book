package com.adventurebook.book.dto;

import com.adventurebook.section.entity.ConsequenceType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ConsequenceRequestDTO(@NotNull ConsequenceType type, @NotNull @Positive Integer value, String text) {
}
