package com.adventurebook.section.entity;

import com.adventurebook.book.entity.Book;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Section {

    @EmbeddedId
    private SectionId id;

    @Setter
    @MapsId("bookId")
    @ManyToOne(fetch = FetchType.LAZY)
    private Book book;

    @Column(name = "text")
    private String text;

    @Enumerated(EnumType.STRING)
    private SectionType type;

    @JdbcTypeCode(SqlTypes.JSON)
    private List<Option> options;

    public Section(int sectionId, String text, SectionType type, List<Option> options) {
        this.id = new SectionId(null, sectionId); // bookId is filled from book by @MapsId
        this.text = text;
        this.type = type;
        this.options = options;
    }
}
