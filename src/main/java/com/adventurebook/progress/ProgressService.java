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
        } else {
            log.info("Player {} restarted book {} after {}", playerId, bookId, progress.getStatus());
            progress.restart(beginSectionId);
        }
        return ProgressDTO.from(progress, begin);
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
        int from = progress.getCurrentSectionId();
        List<Option> options = findSection(bookId, from).getOptions();
        if (options == null || option >= options.size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Section " + from + " has no option " + option);
        }

        Section target = findSection(bookId, options.get(option).gotoId());
        progress.moveTo(target.getId().sectionId(), target.getType() == SectionType.END);
        log.info("Player {} chose option {} in section {} of book {} -> {}",
                playerId, option, from, bookId, target.getId().sectionId());
        if (progress.getStatus() == ProgressStatus.FINISHED) {
            log.info("Player {} finished book {}", playerId, bookId);
        }
        return ProgressDTO.from(progress, target);
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
