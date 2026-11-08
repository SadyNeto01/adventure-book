package com.adventurebook.book;

import com.adventurebook.book.entity.Book;
import com.adventurebook.section.entity.Option;
import com.adventurebook.section.entity.Section;
import com.adventurebook.section.entity.SectionType;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class BookValidator {

    public List<String> validate(Book book) {
        List<String> errors = new ArrayList<>();
        List<Section> sections = book.getSections();

        long begins = sections.stream().filter(s -> s.getType() == SectionType.BEGIN).count();
        if (begins != 1) {
            errors.add("Book must have exactly one BEGIN section, found " + begins);
        }
        if (sections.stream().noneMatch(s -> s.getType() == SectionType.END)) {
            errors.add("Book must have at least one END section");
        }

        Set<Integer> ids = new HashSet<>();
        for (Section section : sections) {
            if (!ids.add(section.getId().sectionId())) {
                errors.add("Section " + section.getId().sectionId() + " is repeated");
            }
        }

        for (Section section : sections) {
            int id = section.getId().sectionId();
            List<Option> options = section.getOptions() == null ? List.of() : section.getOptions();
            if (section.getType() != SectionType.END && options.isEmpty()) {
                errors.add("Section " + id + " is " + section.getType() + " but has no options");
            }
            for (Option option : options) {
                if (!ids.contains(option.gotoId())) {
                    errors.add("Section " + id + " has an option to missing section " + option.gotoId());
                }
            }
        }
        return errors;
    }
}
