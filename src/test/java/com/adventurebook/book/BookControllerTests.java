package com.adventurebook.book;

import com.adventurebook.TestcontainersConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.List;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@AutoConfigureMockMvc
class BookControllerTests {

	@Autowired
	MockMvc mvc;

	@Test
	void listsSeededBooksSortedByTitleWithCategories() throws Exception {
		mvc.perform(get("/books"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content[*].title")
						.value(contains("Pirates of the Jade Sea", "The Crystal Caverns", "The Prisoner")))
				.andExpect(jsonPath("$.content[1].categories").value(contains("ADVENTURE", "FANTASY")))
				.andExpect(jsonPath("$.page.totalElements").value(3));
	}

	@Test
	void paginatesBooks() throws Exception {
		mvc.perform(get("/books").param("size", "1").param("page", "1"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content[*].title").value(contains("The Crystal Caverns")))
				.andExpect(jsonPath("$.page.size").value(1))
				.andExpect(jsonPath("$.page.number").value(1))
				.andExpect(jsonPath("$.page.totalElements").value(3))
				.andExpect(jsonPath("$.page.totalPages").value(3));
	}

	@ParameterizedTest
	@CsvSource(delimiter = ';', value = {
			"title=crystal; The Crystal Caverns",
			"title=CRYSTAL; The Crystal Caverns",
			"author=blackwood; Pirates of the Jade Sea",
			"category=horror; The Prisoner",
			"category=adventure; Pirates of the Jade Sea|The Crystal Caverns",
			"difficulty=MEDIUM; Pirates of the Jade Sea",
			"category=adventure&difficulty=EASY; The Crystal Caverns",
			"category=adventure&difficulty=HARD;",
			"category=adventure&category=fiction; Pirates of the Jade Sea",
			"category=adventure&category=horror;",
			"title=%25;", // a literal "%" must not act as a LIKE wildcard
	})
	void filtersBooks(String query, String expectedTitles) throws Exception {
		List<String> expected = expectedTitles == null ? List.of() : List.of(expectedTitles.split("\\|"));

		mvc.perform(get("/books?" + query))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content[*].title").value(is(expected)));
	}

	@Test
	void paginatesFilteredBooks() throws Exception {
		mvc.perform(get("/books").param("category", "adventure").param("size", "1"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.page.totalElements").value(2))
				.andExpect(jsonPath("$.page.totalPages").value(2));
	}

	@Test
	void rejectsUnknownDifficultyWithBadRequest() throws Exception {
		mvc.perform(get("/books").param("difficulty", "INVALIDO"))
				.andExpect(status().isBadRequest())
				.andExpect(content().contentType("application/problem+json"));
	}

	@Test
	void returnsBookDetailsWithBeginSection() throws Exception {
		mvc.perform(get("/books/1"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.title").value("The Crystal Caverns"))
				.andExpect(jsonPath("$.author").value("Evelyn Stormrider"))
				.andExpect(jsonPath("$.difficulty").value("EASY"))
				.andExpect(jsonPath("$.categories").value(contains("ADVENTURE", "FANTASY")))
				.andExpect(jsonPath("$.beginSectionId").value(1));
	}

	@Test
	void returnsNotFoundForUnknownBook() throws Exception {
		mvc.perform(get("/books/999"))
				.andExpect(status().isNotFound())
				.andExpect(content().contentType("application/problem+json"))
				.andExpect(jsonPath("$.detail").value("Book 999 not found"));
	}

	@Test
	void rejectsNonNumericBookIdWithBadRequest() throws Exception {
		mvc.perform(get("/books/abc"))
				.andExpect(status().isBadRequest())
				.andExpect(content().contentType("application/problem+json"));
	}

	@Test
	void addsNormalizedCategoryOnceAndRemovesIt() throws Exception {
		addCategory(1, " horror ")
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.categories").value(contains("ADVENTURE", "FANTASY", "HORROR")));
		addCategory(1, "HORROR")
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.detail").value("Book 1 already has category HORROR"));

		mvc.perform(delete("/books/1/categories/horror"))
				.andExpect(status().isNoContent());
		mvc.perform(get("/books/1"))
				.andExpect(jsonPath("$.categories").value(contains("ADVENTURE", "FANTASY")));
	}

	@Test
	void removingMissingCategoryIsNotFound() throws Exception {
		mvc.perform(delete("/books/1/categories/sci-fi"))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.detail").value("Book 1 has no category SCI-FI"));
	}

	@Test
	void rejectsBlankOrTooLongCategoryWithErrors() throws Exception {
		for (String category : List.of(" ", "x".repeat(51))) {
			addCategory(1, category)
					.andExpect(status().isBadRequest())
					.andExpect(content().contentType("application/problem+json"))
					.andExpect(jsonPath("$.errors[0].field").value("category"));
		}
	}

	@Test
	void categoryEndpointsReturnNotFoundForUnknownBook() throws Exception {
		addCategory(999, "horror").andExpect(status().isNotFound());
		mvc.perform(delete("/books/999/categories/horror")).andExpect(status().isNotFound());
	}

	@Test
	void rejectsUnknownSortPropertyWithBadRequest() throws Exception {
		mvc.perform(get("/books").param("sort", "string"))
				.andExpect(status().isBadRequest())
				.andExpect(content().contentType("application/problem+json"))
				.andExpect(jsonPath("$.detail").value("Cannot sort by 'string'. Allowed: [id, title, author, difficulty]"));
	}

	private ResultActions addCategory(long bookId, String category) throws Exception {
		return mvc.perform(post("/books/{id}/categories", bookId)
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"category\": \"" + category + "\"}"));
	}
}
