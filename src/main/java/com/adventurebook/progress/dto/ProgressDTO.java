package com.adventurebook.progress.dto;

import com.adventurebook.progress.entity.PlayerProgress;
import com.adventurebook.progress.entity.ProgressStatus;
import com.adventurebook.section.dto.SectionDTO;
import com.adventurebook.section.entity.Consequence;
import com.adventurebook.section.entity.Section;

/** {@code consequence} is the one applied by this request's choice; null on start and get. */
public record ProgressDTO(Long playerId, Long bookId, int health, ProgressStatus status, SectionDTO section,
                          ConsequenceDTO consequence) {

    public static ProgressDTO from(PlayerProgress progress, Section section) {
        return from(progress, section, null);
    }

    public static ProgressDTO from(PlayerProgress progress, Section section, Consequence consequence) {
        return new ProgressDTO(progress.getPlayerId(), progress.getBookId(), progress.getHealth(),
                progress.getStatus(), SectionDTO.from(section), ConsequenceDTO.from(consequence));
    }
}
