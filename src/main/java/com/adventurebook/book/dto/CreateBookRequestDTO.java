package com.adventurebook.book.dto;

import com.adventurebook.book.entity.Difficulty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CreateBookRequestDTO(
        @NotBlank @Size(max = 255) String title,
        @NotBlank @Size(max = 255) String author,
        @NotNull Difficulty difficulty,
        List<@NotBlank @Size(max = 50) String> categories,
        @NotEmpty List<@NotNull @Valid SectionRequestDTO> sections) {
}
