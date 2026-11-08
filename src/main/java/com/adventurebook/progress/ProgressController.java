package com.adventurebook.progress;

import com.adventurebook.progress.dto.ChoiceRequestDTO;
import com.adventurebook.progress.dto.ProgressDTO;
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

import java.util.List;

@RestController
@RequestMapping("/players/{playerId}/books")
@RequiredArgsConstructor
public class ProgressController {

    private final ProgressService progressService;

    @GetMapping
    public List<ProgressDTO> list(@PathVariable Long playerId) {
        return progressService.list(playerId);
    }

    @PostMapping("/{bookId}")
    @ResponseStatus(HttpStatus.CREATED)
    public ProgressDTO start(@PathVariable Long playerId, @PathVariable Long bookId) {
        return progressService.start(playerId, bookId);
    }

    @GetMapping("/{bookId}")
    public ProgressDTO get(@PathVariable Long playerId, @PathVariable Long bookId) {
        return progressService.get(playerId, bookId);
    }

    @PostMapping("/{bookId}/choices")
    public ProgressDTO choose(@PathVariable Long playerId, @PathVariable Long bookId,
                              @Valid @RequestBody ChoiceRequestDTO request) {
        return progressService.choose(playerId, bookId, request.option());
    }

    @PostMapping("/{bookId}/pause")
    public ProgressDTO pause(@PathVariable Long playerId, @PathVariable Long bookId) {
        return progressService.pause(playerId, bookId);
    }

    @PostMapping("/{bookId}/resume")
    public ProgressDTO resume(@PathVariable Long playerId, @PathVariable Long bookId) {
        return progressService.resume(playerId, bookId);
    }
}
