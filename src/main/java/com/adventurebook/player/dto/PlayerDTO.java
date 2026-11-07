package com.adventurebook.player.dto;

import com.adventurebook.player.entity.Player;

public record PlayerDTO(Long id, String name) {

    public static PlayerDTO from(Player player) {
        return new PlayerDTO(player.getId(), player.getName());
    }
}
