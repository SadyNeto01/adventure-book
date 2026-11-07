package com.adventurebook.player;

import com.adventurebook.player.dto.PlayerDTO;
import com.adventurebook.player.entity.Player;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlayerService {

    private final PlayerRepository players;

    @Transactional
    public PlayerDTO create(String name) {
        Player player = players.save(new Player(name.trim()));
        log.info("Player {} created", player.getId());
        return PlayerDTO.from(player);
    }

    @Transactional(readOnly = true)
    public PlayerDTO get(Long id) {
        log.info("Get player id={}", id);
        return players.findById(id)
                .map(PlayerDTO::from)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Player " + id + " not found"));
    }
}
