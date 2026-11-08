package com.adventurebook.book;

import com.adventurebook.TestcontainersConfiguration;
import com.adventurebook.book.entity.Book;
import com.adventurebook.book.entity.Difficulty;
import com.adventurebook.section.entity.Consequence;
import com.adventurebook.section.entity.ConsequenceType;
import com.adventurebook.section.entity.Option;
import com.adventurebook.section.entity.Section;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@Transactional
class SeedBooksTests {

	@Autowired
	BookRepository books;

	@Autowired
	BookValidator validator;

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
			assertThat(validator.validate(book)).as(book.getTitle()).isEmpty();
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
