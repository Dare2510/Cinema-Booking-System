package com.dare.cinema_booking_system.integration;

import com.dare.cinema_booking_system.movies.dto.MovieRequest;
import com.dare.cinema_booking_system.movies.entity.Genre;
import com.dare.cinema_booking_system.movies.repository.MovieRepository;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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

	@Autowired
	private MovieRepository movieRepository;


	@AfterEach
	public void tearDown() throws Exception {
		movieRepository.deleteAll();
	}


	@Test
	public void createAndGetMovie_whenJsonIsValid_returnIsCreatedAndIsOK() throws Exception {
		MovieRequest request = new MovieRequest(
				"testTitle", "testDescription", 100, Genre.FANTASY);

		String responseJson = mockMvc.perform(post("/api/movies")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isCreated())
				.andReturn().getResponse().getContentAsString();

		int id = JsonPath.read(responseJson, "$.id");

		mockMvc.perform(get("/api/movies/"+id))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.title").value("testTitle"));
	}

	@Test
	public void createMovie_whenJsonIsInvalid_returnBadRequest() throws Exception {
		MovieRequest request = new MovieRequest(
				" ", "testDescription", 100, Genre.FANTASY);

		mockMvc.perform(post("/api/movies")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value("Title is required"));
	}

	@Test
	public void getMovie_whenMovieNotExists_returnNotFound() throws Exception {
		mockMvc.perform(get("/api/movies/999"))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.message")
						.value("Could not find movie with id: 999"));
	}

	@Test
	public void getMovieListByGenre_whenMoviesExist_returnIsOK() throws Exception {
		MovieRequest request = new MovieRequest(
				"testTitle", "testDescription", 100, Genre.FANTASY);

		mockMvc.perform(post("/api/movies")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isCreated());

		mockMvc.perform(get("/api/movies/filter/genre/FANTASY"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].title").value("testTitle"));
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
		MovieRequest request = new MovieRequest(
				"testTitle", "testDescription", 100, Genre.FANTASY);

		mockMvc.perform(post("/api/movies")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
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

	@Test
	public void updateMovie_whenMoviesExistAndRequestIsValid_returnIsOK() throws Exception {
		MovieRequest request = new MovieRequest(
				"testTitle", "testDescription", 100, Genre.FANTASY);

		String responseJson = mockMvc.perform(post("/api/movies")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isCreated())
				.andReturn().getResponse().getContentAsString();

		int id = JsonPath.read(responseJson, "$.id");

		request.setTitle("newTitle");

		mockMvc.perform(patch("/api/movies/" + id)
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.title").value("newTitle"));

	}

	@Test
	public void deleteMovie_whenMoviesExistAndRequestIsValid_returnNoContent() throws Exception {
		MovieRequest request = new MovieRequest(
				"testTitle", "testDescription", 100, Genre.FANTASY);

		String responseJson = mockMvc.perform(post("/api/movies")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isCreated())
				.andReturn().getResponse().getContentAsString();

		int id = JsonPath.read(responseJson, "$.id");

		mockMvc.perform(delete("/api/movies/" + id))
				.andExpect(status().isNoContent());

	}

	@Test
	public void getPageOfMovies_withPageableDefaults_returnIsOK() throws Exception {
		mockMvc.perform(get("/api/movies")
						.param("page", "0")
						.param("size", "10")
						.param("sort", "title")
						.param("direction", "ASC"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content").isArray())
				.andExpect(jsonPath("$.size").value(10));
	}

}
