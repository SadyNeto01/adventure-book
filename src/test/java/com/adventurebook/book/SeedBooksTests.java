package com.adventurebook.book;

import com.adventurebook.TestcontainersConfiguration;
import com.adventurebook.book.entity.Book;
import com.adventurebook.book.entity.Difficulty;
import com.adventurebook.section.entity.Consequence;
import com.adventurebook.section.entity.ConsequenceType;
import com.adventurebook.section.entity.Option;
import com.adventurebook.section.entity.Section;
import com.adventurebook.section.entity.SectionType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@Transactional
class SeedBooksTests {

	@Autowired
	BookRepository books;

	@Test
	void seedLoadsTheThreeFixedSampleBooks() {
		assertThat(books.count()).isEqualTo(3);

		Book crystal = books.findById(1L).orElseThrow();
		assertThat(crystal.getTitle()).isEqualTo("The Crystal Caverns");
		assertThat(crystal.getDifficulty()).isEqualTo(Difficulty.EASY);
		assertThat(crystal.getCategories()).containsExactlyInAnyOrder("ADVENTURE", "FANTASY");
		assertThat(crystal.getSections()).hasSize(13);

		assertThat(books.findById(2L).orElseThrow().getSections()).hasSize(17);
		assertThat(books.findById(3L).orElseThrow().getSections()).hasSize(5);
	}

	@Test
	void seededOptionsAndConsequencesAreReadBackAsRecords() {
		Section crevice = section(books.findById(1L).orElseThrow(), 20);

		assertThat(crevice.getId().bookId()).isEqualTo(1L);
		assertThat(crevice.getOptions()).containsExactly(
				new Option("Enter the crevice", 200,
						new Consequence(ConsequenceType.LOSE_HEALTH, 4, "You scrape your shoulder squeezing through the narrow gap.")),
				new Option("Return to the entrance", 1, null));
	}

	@Test
	void seededBooksAreValid() {
		for (Book book : books.findAll()) {
			Set<Integer> ids = book.getSections().stream()
					.map(s -> s.getId().sectionId()).collect(Collectors.toSet());

			assertThat(book.getSections()).filteredOn(s -> s.getType() == SectionType.BEGIN).hasSize(1);
			assertThat(book.getSections()).anyMatch(s -> s.getType() == SectionType.END);
			for (Section s : book.getSections()) {
				if (s.getType() != SectionType.END) {
					assertThat(s.getOptions()).as("%s section %s", book.getTitle(), s.getId().sectionId()).isNotEmpty();
					assertThat(s.getOptions()).allMatch(o -> ids.contains(o.gotoId()));
				}
			}
		}
	}

	@Test
	void nextBookIdComesAfterTheSeed() {
		Book saved = books.saveAndFlush(new Book("New", "Author", Difficulty.EASY));

		assertThat(saved.getId()).isGreaterThan(3L);
	}

	private static Section section(Book book, int sectionId) {
		return book.getSections().stream()
				.filter(s -> s.getId().sectionId() == sectionId).findFirst().orElseThrow();
	}
}
