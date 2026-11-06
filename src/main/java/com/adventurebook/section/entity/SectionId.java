package com.adventurebook.section.entity;

import jakarta.persistence.Embeddable;

import java.io.Serializable;

@Embeddable
public record SectionId(Long bookId, int sectionId) implements Serializable {
}
