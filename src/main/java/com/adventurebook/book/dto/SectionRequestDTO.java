package com.adventurebook.book.dto;

import com.adventurebook.section.entity.SectionType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record SectionRequestDTO(
        @NotNull Integer id,
        @NotBlank String text,
        @NotNull SectionType type,
        List<@NotNull @Valid OptionRequestDTO> options) {
}
