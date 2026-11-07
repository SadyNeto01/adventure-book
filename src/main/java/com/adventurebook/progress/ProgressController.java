package com.adventurebook.progress;

import com.adventurebook.progress.dto.ProgressDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/players/{playerId}/books/{bookId}")
@RequiredArgsConstructor
public class ProgressController {

    private final ProgressService progressService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProgressDTO start(@PathVariable Long playerId, @PathVariable Long bookId) {
        return progressService.start(playerId, bookId);
    }

    @GetMapping
    public ProgressDTO get(@PathVariable Long playerId, @PathVariable Long bookId) {
        return progressService.get(playerId, bookId);
    }
}
