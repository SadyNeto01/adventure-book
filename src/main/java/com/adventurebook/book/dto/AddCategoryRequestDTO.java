package com.adventurebook.book.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AddCategoryRequestDTO(@NotBlank @Size(max = 50) String category) {
}
