package com.adventurebook.player;

import com.adventurebook.player.dto.CreatePlayerRequestDTO;
import com.adventurebook.player.dto.PlayerDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/players")
@RequiredArgsConstructor
public class PlayerController {

    private final PlayerService playerService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PlayerDTO create(@Valid @RequestBody CreatePlayerRequestDTO request) {
        return playerService.create(request.name());
    }

    @GetMapping("/{id}")
    public PlayerDTO get(@PathVariable Long id) {
        return playerService.get(id);
    }
}
