package com.adventurebook.section;

import com.adventurebook.section.entity.Section;
import com.adventurebook.section.entity.SectionId;
import com.adventurebook.section.entity.SectionType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SectionRepository extends JpaRepository<Section, SectionId> {

    Optional<Section> findByIdBookIdAndType(Long bookId, SectionType type);
}
