package com.adventurebook.player.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreatePlayerRequestDTO(@NotBlank @Size(max = 100) String name) {
}
