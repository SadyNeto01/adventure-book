package com.adventurebook.book;

import com.adventurebook.book.entity.Book;
import com.adventurebook.book.entity.Difficulty;
import com.adventurebook.section.entity.Option;
import com.adventurebook.section.entity.Section;
import com.adventurebook.section.entity.SectionType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BookValidatorTests {

	final BookValidator validator = new BookValidator();

	@Test
	void validBookHasNoReasons() {
		assertThat(validator.validate(book(begin(1, 2), end(2)))).isEmpty();
	}

	@Test
	void requiresExactlyOneBeginSection() {
		assertThat(validator.validate(book(node(1, 2), end(2))))
				.containsExactly("Book must have exactly one BEGIN section, found 0");
		assertThat(validator.validate(book(begin(1, 3), begin(2, 3), end(3))))
				.containsExactly("Book must have exactly one BEGIN section, found 2");
	}

	@Test
	void requiresAnEndSection() {
		assertThat(validator.validate(book(begin(1, 2), node(2, 1))))
				.containsExactly("Book must have at least one END section");
	}

	@Test
	void rejectsOptionToMissingSection() {
		assertThat(validator.validate(book(begin(1, 99), end(2))))
				.containsExactly("Section 1 has an option to missing section 99");
	}

	@Test
	void rejectsNonEndSectionWithoutOptions() {
		assertThat(validator.validate(book(begin(1, 2), section(2, SectionType.NODE, null), end(3))))
				.containsExactly("Section 2 is NODE but has no options");
		assertThat(validator.validate(book(begin(1, 2), section(2, SectionType.NODE, List.of()), end(3))))
				.containsExactly("Section 2 is NODE but has no options");
	}

	@Test
	void rejectsRepeatedSectionIds() {
		assertThat(validator.validate(book(begin(1, 2), node(2, 3), end(2), end(3))))
				.containsExactly("Section 2 is repeated");
	}

	@Test
	void reportsEveryViolation() {
		assertThat(validator.validate(book(node(1, 99), section(2, SectionType.NODE, null))))
				.containsExactlyInAnyOrder(
						"Book must have exactly one BEGIN section, found 0",
						"Book must have at least one END section",
						"Section 1 has an option to missing section 99",
						"Section 2 is NODE but has no options");
	}

	private static Book book(Section... sections) {
		Book book = new Book("Title", "Author", Difficulty.EASY);
		for (Section section : sections) {
			book.addSection(section);
		}
		return book;
	}

	private static Section begin(int id, int gotoId) {
		return section(id, SectionType.BEGIN, List.of(new Option("Go", gotoId, null)));
	}

	private static Section node(int id, int gotoId) {
		return section(id, SectionType.NODE, List.of(new Option("Go", gotoId, null)));
	}

	private static Section end(int id) {
		return section(id, SectionType.END, null);
	}

	private static Section section(int id, SectionType type, List<Option> options) {
		return new Section(id, "Text", type, options);
	}
}
