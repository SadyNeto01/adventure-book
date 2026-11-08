package com.adventurebook.progress;

import com.adventurebook.TestcontainersConfiguration;
import com.adventurebook.player.PlayerRepository;
import com.adventurebook.player.entity.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@AutoConfigureMockMvc
class ProgressControllerTests {

	@Autowired
	MockMvc mvc;

	@Autowired
	PlayerRepository players;

	@Autowired
	JdbcTemplate jdbc;

	long playerId;

	@BeforeEach
	void createPlayer() {
		playerId = players.save(new Player("Reader")).getId();
	}

	@Test
	void startsBookAtBeginWithFullHealth() throws Exception {
		mvc.perform(post("/players/{p}/books/1", playerId))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.playerId").value(playerId))
				.andExpect(jsonPath("$.bookId").value(1))
				.andExpect(jsonPath("$.health").value(10))
				.andExpect(jsonPath("$.status").value("IN_PROGRESS"))
				.andExpect(jsonPath("$.section.id").value(1))
				.andExpect(jsonPath("$.section.type").value("BEGIN"));

		mvc.perform(get("/players/{p}/books/1", playerId))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("IN_PROGRESS"))
				.andExpect(jsonPath("$.section.id").value(1));
	}

	@Test
	void rejectsStartingBookAlreadyInProgress() throws Exception {
		mvc.perform(post("/players/{p}/books/1", playerId)).andExpect(status().isCreated());

		mvc.perform(post("/players/{p}/books/1", playerId))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.detail").value("Player " + playerId + " is already reading book 1"));
	}

	@ParameterizedTest
	@ValueSource(strings = {"DEAD", "FINISHED"})
	void restartsEndedProgressInPlace(String endedStatus) throws Exception {
		mvc.perform(post("/players/{p}/books/1", playerId)).andExpect(status().isCreated());
		jdbc.update("UPDATE player_progress SET status = ?, health = 0, current_section_id = 800 WHERE player_id = ?",
				endedStatus, playerId);

		mvc.perform(post("/players/{p}/books/1", playerId))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.health").value(10))
				.andExpect(jsonPath("$.status").value("IN_PROGRESS"))
				.andExpect(jsonPath("$.section.id").value(1));

		assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM player_progress WHERE player_id = ?", Integer.class, playerId))
				.isEqualTo(1);
	}

	@Test
	void playerCanReadDifferentBooks() throws Exception {
		mvc.perform(post("/players/{p}/books/1", playerId)).andExpect(status().isCreated());
		mvc.perform(post("/players/{p}/books/2", playerId)).andExpect(status().isCreated());
	}

	@Test
	void returnsNotFoundForMissingProgressPlayerOrBook() throws Exception {
		mvc.perform(get("/players/{p}/books/1", playerId))
				.andExpect(status().isNotFound())
				.andExpect(content().contentType("application/problem+json"));
		mvc.perform(post("/players/999999/books/1"))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.detail").value("Player 999999 not found"));
		mvc.perform(post("/players/{p}/books/999", playerId))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.detail").value("Book 999 not found"));
	}

	@Test
	void choosingFirstOptionFromBeginFinishesTheBook() throws Exception {
		mvc.perform(post("/players/{p}/books/1", playerId)).andExpect(status().isCreated());

		for (int expected : new int[] {100, 300, 600, 900}) {
			choose(1, "0")
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.section.id").value(expected))
					.andExpect(jsonPath("$.status").value("IN_PROGRESS"));
		}
		choose(1, "0")
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.section.id").value(800))
				.andExpect(jsonPath("$.section.type").value("END"))
				.andExpect(jsonPath("$.status").value("FINISHED"));
	}

	@Test
	void rejectsOptionNotInCurrentSectionAndKeepsProgress() throws Exception {
		mvc.perform(post("/players/{p}/books/1", playerId)).andExpect(status().isCreated());

		choose(1, "9")
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.detail").value("Section 1 has no option 9"));
		mvc.perform(get("/players/{p}/books/1", playerId))
				.andExpect(jsonPath("$.section.id").value(1));
	}

	@ParameterizedTest
	@ValueSource(strings = {"null", "-1"})
	void rejectsMissingOrNegativeOptionWithErrors(String option) throws Exception {
		mvc.perform(post("/players/{p}/books/1", playerId)).andExpect(status().isCreated());

		choose(1, option)
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.errors[0].field").value("option"));
	}

	@Test
	void choosingWithoutStartingIsNotFound() throws Exception {
		choose(1, "0").andExpect(status().isNotFound());
	}

	private ResultActions choose(long bookId, String option) throws Exception {
		return mvc.perform(post("/players/{p}/books/{b}/choices", playerId, bookId)
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"option\": " + option + "}"));
	}
}
