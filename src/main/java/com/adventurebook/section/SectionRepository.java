package com.adventurebook.section;

import com.adventurebook.section.entity.Section;
import com.adventurebook.section.entity.SectionId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SectionRepository extends JpaRepository<Section, SectionId> {
}
