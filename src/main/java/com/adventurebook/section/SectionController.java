package com.adventurebook.section;

import com.adventurebook.section.dto.SectionDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/books/{bookId}/sections")
@RequiredArgsConstructor
public class SectionController {

    private final SectionService sectionService;

    @GetMapping("/{sectionId}")
    public SectionDTO read(@PathVariable Long bookId, @PathVariable int sectionId) {
        return sectionService.read(bookId, sectionId);
    }
}
