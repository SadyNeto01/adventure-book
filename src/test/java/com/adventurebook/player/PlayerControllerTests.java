package com.adventurebook.player;

import com.adventurebook.TestcontainersConfiguration;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@AutoConfigureMockMvc
class PlayerControllerTests {

	@Autowired
	MockMvc mvc;

	@Test
	void createsPlayerWithTrimmedNameAndReadsItBack() throws Exception {
		long id = createdId(createPlayer(" Sady ")
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.name").value("Sady")));

		mvc.perform(get("/players/{id}", id))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(id))
				.andExpect(jsonPath("$.name").value("Sady"));
	}

	@Test
	void allowsPlayersWithTheSameName() throws Exception {
		long first = createdId(createPlayer("Twin").andExpect(status().isCreated()));
		long second = createdId(createPlayer("Twin").andExpect(status().isCreated()));

		assertThat(second).isNotEqualTo(first);
	}

	@Test
	void rejectsBlankOrTooLongNameWithErrors() throws Exception {
		for (String name : List.of(" ", "x".repeat(101))) {
			createPlayer(name)
					.andExpect(status().isBadRequest())
					.andExpect(jsonPath("$.errors[0].field").value("name"));
		}
	}

	@Test
	void returnsNotFoundForUnknownPlayer() throws Exception {
		mvc.perform(get("/players/999"))
				.andExpect(status().isNotFound())
				.andExpect(content().contentType("application/problem+json"))
				.andExpect(jsonPath("$.detail").value("Player 999 not found"));
	}

	private ResultActions createPlayer(String name) throws Exception {
		return mvc.perform(post("/players")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"name\": \"" + name + "\"}"));
	}

	private static long createdId(ResultActions result) throws Exception {
		return ((Number) JsonPath.read(result.andReturn().getResponse().getContentAsString(), "$.id")).longValue();
	}
}
