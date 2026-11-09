package com.adventurebook.book.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record OptionRequestDTO(@NotBlank String description, @NotNull Integer gotoId, @Valid ConsequenceRequestDTO consequence) {
}
