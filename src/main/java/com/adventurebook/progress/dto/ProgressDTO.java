package com.adventurebook.progress.dto;

import com.adventurebook.progress.entity.PlayerProgress;
import com.adventurebook.progress.entity.ProgressStatus;
import com.adventurebook.section.dto.SectionDTO;
import com.adventurebook.section.entity.Section;

public record ProgressDTO(Long playerId, Long bookId, int health, ProgressStatus status, SectionDTO section) {

    public static ProgressDTO from(PlayerProgress progress, Section section) {
        return new ProgressDTO(progress.getPlayerId(), progress.getBookId(), progress.getHealth(),
                progress.getStatus(), SectionDTO.from(section));
    }
}
