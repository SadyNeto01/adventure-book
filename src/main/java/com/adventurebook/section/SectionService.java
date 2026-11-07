package com.adventurebook.section;

import com.adventurebook.section.dto.SectionDTO;
import com.adventurebook.section.entity.SectionId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@Service
@RequiredArgsConstructor
public class SectionService {

    private final SectionRepository sections;

    @Transactional(readOnly = true)
    public SectionDTO read(Long bookId, int sectionId) {
        log.info("Reading section {} of book {}", sectionId, bookId);
        SectionDTO section = sections.findById(new SectionId(bookId, sectionId))
                .map(SectionDTO::from)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Section " + sectionId + " of book " + bookId + " not found"));
        log.debug("Section {} of book {} is {} with {} options", sectionId, bookId, section.type(), section.options().size());
        return section;
    }
}
