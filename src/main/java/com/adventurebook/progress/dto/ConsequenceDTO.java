package com.adventurebook.progress.dto;

import com.adventurebook.section.entity.Consequence;
import com.adventurebook.section.entity.ConsequenceType;

public record ConsequenceDTO(ConsequenceType type, int value, String text) {

    public static ConsequenceDTO from(Consequence consequence) {
        return consequence == null ? null
                : new ConsequenceDTO(consequence.type(), consequence.value(), consequence.text());
    }
}
