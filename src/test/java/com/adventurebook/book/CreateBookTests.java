package com.adventurebook.book;

import com.adventurebook.TestcontainersConfiguration;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@AutoConfigureMockMvc
class CreateBookTests {

	private static final String VALID_SECTIONS = """
			[
			  {"id": 1, "text": "Start", "type": "BEGIN", "options": [
			    {"description": "Go on", "gotoId": 2,
			     "consequence": {"type": "LOSE_HEALTH", "value": 3, "text": "Ouch"}}]},
			  {"id": 2, "text": "The end", "type": "END"}
			]""";

	@Autowired
	MockMvc mvc;

	@Autowired
	JdbcTemplate jdbc;

	@AfterEach
	void removeCreatedBooks() {
		jdbc.update("DELETE FROM book WHERE id > 3");
	}

	@Test
	void createsValidBookAndServesIt() throws Exception {
		MvcResult result = create("New Book", "New Author", "[\"adventure\", \" Horror \"]", VALID_SECTIONS)
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.title").value("New Book"))
				.andExpect(jsonPath("$.categories").value(contains("ADVENTURE", "HORROR")))
				.andExpect(jsonPath("$.beginSectionId").value(1))
				.andReturn();
		int id = JsonPath.read(result.getResponse().getContentAsString(), "$.id");

		mvc.perform(get("/books/{id}", id)).andExpect(status().isOk());
		mvc.perform(get("/books/{id}/sections/1", id))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.options[0].gotoId").value(2));
	}

	@Test
	void rejectsInvalidBookWithEveryErrorAndSavesNothing() throws Exception {
		String sections = """
				[{"id": 1, "text": "Start", "type": "NODE", "options": [{"description": "Go", "gotoId": 99}]}]""";

		create("Broken", "Author", "[]", sections)
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.detail").value("Book is invalid"))
				.andExpect(jsonPath("$.errors").value(hasItem("Book must have exactly one BEGIN section, found 0")))
				.andExpect(jsonPath("$.errors").value(hasItem("Book must have at least one END section")))
				.andExpect(jsonPath("$.errors").value(hasItem("Section 1 has an option to missing section 99")));

		assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM book", Integer.class)).isEqualTo(3);
	}

	@Test
	void rejectsMissingFieldsWithFieldErrors() throws Exception {
		mvc.perform(post("/books")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"author\": \"Author\", \"difficulty\": \"EASY\", \"sections\": " + VALID_SECTIONS + "}"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.errors[0].field").value("title"));
	}

	@Test
	void rejectsDuplicateTitleAndAuthorWithConflict() throws Exception {
		create("The Prisoner", "Daniel El Fuego", "[]", VALID_SECTIONS)
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.detail").value("Book 'The Prisoner' by Daniel El Fuego already exists"));
	}

	private ResultActions create(String title, String author, String categories, String sections) throws Exception {
		return mvc.perform(post("/books")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"title": "%s", "author": "%s", "difficulty": "EASY", "categories": %s, "sections": %s}"""
						.formatted(title, author, categories, sections)));
	}
}
