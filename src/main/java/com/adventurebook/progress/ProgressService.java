package com.adventurebook.progress;

import com.adventurebook.player.PlayerRepository;
import com.adventurebook.progress.dto.ProgressDTO;
import com.adventurebook.progress.entity.PlayerProgress;
import com.adventurebook.progress.entity.ProgressStatus;
import com.adventurebook.section.SectionRepository;
import com.adventurebook.section.entity.Option;
import com.adventurebook.section.entity.Section;
import com.adventurebook.section.entity.SectionId;
import com.adventurebook.section.entity.SectionType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProgressService {

    private final PlayerProgressRepository progresses;
    private final PlayerRepository players;
    private final SectionRepository sections;

    @Transactional
    public ProgressDTO start(Long playerId, Long bookId) {
        if (!players.existsById(playerId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Player " + playerId + " not found");
        }
        Section begin = sections.findByIdBookIdAndType(bookId, SectionType.BEGIN)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Book " + bookId + " not found"));
        int beginSectionId = begin.getId().sectionId();

        PlayerProgress progress = progresses.findByPlayerIdAndBookId(playerId, bookId).orElse(null);
        if (progress == null) {
            progress = progresses.save(new PlayerProgress(playerId, bookId, beginSectionId));
            log.info("Player {} started book {}", playerId, bookId);
        } else if (progress.getStatus() == ProgressStatus.IN_PROGRESS) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Player " + playerId + " is already reading book " + bookId);
        } else if (progress.getStatus() == ProgressStatus.PAUSED) {
            throw pausedConflict(playerId, bookId);
        } else {
            log.info("Player {} restarted book {} after {}", playerId, bookId, progress.getStatus());
            progress.restart(beginSectionId);
        }
        return ProgressDTO.from(progress, begin);
    }

    @Transactional(readOnly = true)
    public List<ProgressDTO> list(Long playerId) {
        log.info("Listing books of player {}", playerId);
        if (!players.existsById(playerId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Player " + playerId + " not found");
        }
        List<ProgressDTO> books = progresses.findByPlayerIdOrderByUpdatedAtDesc(playerId).stream()
                .map(progress -> ProgressDTO.from(progress, findSection(progress.getBookId(), progress.getCurrentSectionId())))
                .toList();
        log.debug("Player {} has {} books", playerId, books.size());
        return books;
    }

    @Transactional(readOnly = true)
    public ProgressDTO get(Long playerId, Long bookId) {
        log.info("Get progress of player {} in book {}", playerId, bookId);
        PlayerProgress progress = findProgress(playerId, bookId);
        Section section = findSection(bookId, progress.getCurrentSectionId());
        log.debug("Player {} is at section {} of book {} with health {} ({})",
                playerId, section.getId().sectionId(), bookId, progress.getHealth(), progress.getStatus());
        return ProgressDTO.from(progress, section);
    }

    @Transactional
    public ProgressDTO choose(Long playerId, Long bookId, int option) {
        PlayerProgress progress = findProgress(playerId, bookId);
        if (progress.getStatus() == ProgressStatus.PAUSED) {
            throw pausedConflict(playerId, bookId);
        }
        if (progress.getStatus() != ProgressStatus.IN_PROGRESS) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Game over: player " + playerId + " is " + progress.getStatus() + " in book " + bookId);
        }
        int from = progress.getCurrentSectionId();
        List<Option> options = findSection(bookId, from).getOptions();
        if (options == null || option >= options.size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Section " + from + " has no option " + option);
        }

        Option chosen = options.get(option);
        Section target = findSection(bookId, chosen.gotoId());
        int healthBefore = progress.getHealth();
        progress.moveTo(target.getId().sectionId(), target.getType() == SectionType.END, chosen.consequence());
        log.info("Player {} chose option {} in section {} of book {} -> {}, health {} -> {}",
                playerId, option, from, bookId, target.getId().sectionId(), healthBefore, progress.getHealth());
        if (progress.getStatus() != ProgressStatus.IN_PROGRESS) {
            log.info("Player {} is {} in book {}", playerId, progress.getStatus(), bookId);
        }
        return ProgressDTO.from(progress, target, chosen.consequence());
    }

    @Transactional
    public ProgressDTO pause(Long playerId, Long bookId) {
        PlayerProgress progress = findProgress(playerId, bookId);
        if (progress.getStatus() != ProgressStatus.IN_PROGRESS) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Player " + playerId + " can't pause book " + bookId + ": it is " + progress.getStatus());
        }
        progress.pause();
        log.info("Player {} paused book {}", playerId, bookId);
        return ProgressDTO.from(progress, findSection(bookId, progress.getCurrentSectionId()));
    }

    @Transactional
    public ProgressDTO resume(Long playerId, Long bookId) {
        PlayerProgress progress = findProgress(playerId, bookId);
        if (progress.getStatus() != ProgressStatus.PAUSED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Player " + playerId + " can't resume book " + bookId + ": it is " + progress.getStatus());
        }
        progress.resume();
        log.info("Player {} resumed book {}", playerId, bookId);
        return ProgressDTO.from(progress, findSection(bookId, progress.getCurrentSectionId()));
    }

    private static ResponseStatusException pausedConflict(Long playerId, Long bookId) {
        return new ResponseStatusException(HttpStatus.CONFLICT,
                "Player " + playerId + " has paused book " + bookId + ", resume it first");
    }

    private PlayerProgress findProgress(Long playerId, Long bookId) {
        return progresses.findByPlayerIdAndBookId(playerId, bookId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Player " + playerId + " has not started book " + bookId));
    }

    private Section findSection(Long bookId, int sectionId) {
        return sections.findById(new SectionId(bookId, sectionId)).orElseThrow();
    }
}
