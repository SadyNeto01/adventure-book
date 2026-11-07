package com.adventurebook.section;

import com.adventurebook.TestcontainersConfiguration;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@AutoConfigureMockMvc
class SectionControllerTests {

	@Autowired
	MockMvc mvc;

	@Test
	void readsSectionWithOptionsButNoConsequence() throws Exception {
		mvc.perform(get("/books/1/sections/1"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(1))
				.andExpect(jsonPath("$.type").value("BEGIN"))
				.andExpect(jsonPath("$.text").isNotEmpty())
				.andExpect(jsonPath("$.options").value(not(empty())))
				.andExpect(jsonPath("$.options[0]", not(hasKey("consequence"))));
	}

	@Test
	void endSectionHasNoOptions() throws Exception {
		mvc.perform(get("/books/1/sections/800"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.type").value("END"))
				.andExpect(jsonPath("$.options").value(empty()));
	}

	@Test
	void returnsNotFoundForUnknownSectionOrBook() throws Exception {
		for (String path : List.of("/books/1/sections/2", "/books/999/sections/1")) {
			mvc.perform(get(path))
					.andExpect(status().isNotFound())
					.andExpect(content().contentType("application/problem+json"));
		}
	}

	@Test
	void followingFirstOptionFromBeginReachesEnd() throws Exception {
		String book = mvc.perform(get("/books/1")).andReturn().getResponse().getContentAsString();
		int sectionId = JsonPath.read(book, "$.beginSectionId");
		List<Integer> path = new ArrayList<>(List.of(sectionId));

		for (int step = 0; step < 20; step++) {
			String section = mvc.perform(get("/books/1/sections/{id}", sectionId))
					.andExpect(status().isOk())
					.andReturn().getResponse().getContentAsString();
			if (JsonPath.read(section, "$.type").equals("END")) {
				break;
			}
			sectionId = JsonPath.read(section, "$.options[0].gotoId");
			path.add(sectionId);
		}

		assertThat(path).containsExactly(1, 100, 300, 600, 900, 800);
	}
}
