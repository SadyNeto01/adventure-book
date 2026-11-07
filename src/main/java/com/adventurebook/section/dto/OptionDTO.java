package com.adventurebook.section.dto;

import com.adventurebook.section.entity.Option;

public record OptionDTO(String description, int gotoId) {

    public static OptionDTO from(Option option) {
        return new OptionDTO(option.description(), option.gotoId());
    }
}
