package com.adventurebook.section.dto;

import com.adventurebook.section.entity.Section;
import com.adventurebook.section.entity.SectionType;

import java.util.List;

public record SectionDTO(int id, String text, SectionType type, List<OptionDTO> options) {

    public static SectionDTO from(Section section) {
        List<OptionDTO> options = section.getOptions() == null ? List.of()
                : section.getOptions().stream().map(OptionDTO::from).toList();
        return new SectionDTO(section.getId().sectionId(), section.getText(), section.getType(), options);
    }
}
