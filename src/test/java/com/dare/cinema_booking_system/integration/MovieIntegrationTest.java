package com.dare.cinema_booking_system.integration;

import com.dare.cinema_booking_system.movies.dto.MovieRequest;
import com.dare.cinema_booking_system.movies.entity.Genre;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class MovieIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Test
	public void createAndGetMovie_whenJsonIsValid_returnIsCreatedAndIsOK() throws Exception {
		MovieRequest movieRequest = new MovieRequest(
				"testTitle", "testDescription", 100, Genre.FANTASY);

		mockMvc.perform(post("/api/movies")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(movieRequest)))
				.andExpect(status().isCreated());

		mockMvc.perform(get("/api/movies/1"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.title").value("testTitle"));
	}

	@Test
	public void createMovie_whenJsonIsInvalid_returnBadRequest() throws Exception {
		MovieRequest movieRequest = new MovieRequest(
				"", "testDescription", 100, Genre.FANTASY);

		mockMvc.perform(post("/api/movies")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(movieRequest)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value("Title is required"));;
	}

	@Test
	public void getMovieListByGenre_whenMoviesExist_returnIsOK() throws Exception {
		MovieRequest movieRequest = new MovieRequest(
				"testTitle", "testDescription", 100, Genre.FANTASY);

		mockMvc.perform(post("/api/movies")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(movieRequest)))
				.andExpect(status().isCreated());

		mockMvc.perform(get("/api/movies/filter/genre/FANTASY"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.[0]title").value("testTitle"));
	}

	@Test
	public void getMovieListByGenre_whenMoviesNotExist_returnNotFound() throws Exception {
		mockMvc.perform(get("/api/movies/filter/genre/FANTASY"))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.message")
						.value("No movies with FANTASY as genre were found"));
	}

	@Test
	public void getMovieListByDuration_whenMoviesExist_returnIsOK() throws Exception {
		MovieRequest movieRequest = new MovieRequest(
				"testTitle", "testDescription", 100, Genre.FANTASY);

		mockMvc.perform(post("/api/movies")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(movieRequest)))
				.andExpect(status().isCreated());

		mockMvc.perform(get("/api/movies/filter/duration/80"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.[0]title").value("testTitle"));
	}

	@Test
	public void getMovieListByDuration_whenMoviesNotExist_returnNotFound() throws Exception {
		mockMvc.perform(get("/api/movies/filter/duration/80"))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.message")
						.value("No movies with greater than 80 min duration found"));
	}


}
