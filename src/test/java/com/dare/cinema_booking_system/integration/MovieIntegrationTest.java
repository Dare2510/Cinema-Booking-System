package com.dare.cinema_booking_system.integration;

import com.dare.cinema_booking_system.movie.dto.MovieRequest;
import com.dare.cinema_booking_system.movie.entity.Genre;
import com.dare.cinema_booking_system.movie.repository.MovieRepository;
import com.dare.cinema_booking_system.rooms.dto.CinemaRoomRequest;
import com.dare.cinema_booking_system.rooms.repository.CinemaRoomRepository;
import com.dare.cinema_booking_system.screenings.dto.ScreeningRequest;
import com.dare.cinema_booking_system.screenings.entity.TimeSlot;
import com.dare.cinema_booking_system.screenings.repository.ScreeningRepository;
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

import java.math.BigDecimal;
import java.time.LocalDate;

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

	@Autowired
	private ScreeningRepository screeningRepository;

	@Autowired
	private CinemaRoomRepository cinemaRoomRepository;


	@AfterEach
	public void tearDown() throws Exception {
		movieRepository.deleteAll();
		cinemaRoomRepository.deleteAll();
		screeningRepository.deleteAll();
	}

	@Test
	public void createAndGetMovie_whenJsonIsValid_returnIsCreatedAndIsOK() throws Exception {
		MovieRequest request = new MovieRequest(
				"testTitle", "testDescription", 100, Genre.FANTASY);

		Long id = getMovieId(request);

		mockMvc.perform(get("/api/movies/" + id))
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
		mockMvc.perform(get("/api/movies/filter/duration/120"))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.message")
						.value("No movies with greater than 120 min duration found"));
	}

	@Test
	public void updateMovie_whenMoviesExistAndRequestIsValidAndNoScreeningExits_returnIsOK() throws Exception {
		MovieRequest request = new MovieRequest(
				"testTitle", "testDescription", 100, Genre.FANTASY);

		Long id = getMovieId(request);

		request.setTitle("newTitle");

		mockMvc.perform(patch("/api/movies/" + id)
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.title").value("newTitle"));

	}

	@Test
	public void updateMovie_whenMoviesExistAndRequestIsValidAndScreeningExits_returnsBadRequest() throws Exception {
		MovieRequest request = new MovieRequest(
				"testTitle", "testDescription", 100, Genre.FANTASY);

		CinemaRoomRequest roomRequest = new CinemaRoomRequest(10, 10, 20);
		Long movieId = getMovieId(request);
		Long roomId = getCinemaId(roomRequest);

		ScreeningRequest screeningRequest = new ScreeningRequest(roomId, movieId, LocalDate.now(), TimeSlot.PRIME, BigDecimal.TEN);

		mockMvc.perform(post("/api/screening")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(screeningRequest)))
				.andExpect(status().isOk());

		request.setTitle("newTitle");

		mockMvc.perform(patch("/api/movies/" + movieId)
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").
						value("Movie with id " + movieId + " cannot be updated, screening exits"));

	}

	@Test
	public void deleteMovie_whenMoviesExistAndRequestIsValidAndNoScreeningExists_returnNoContent() throws Exception {
		MovieRequest request = new MovieRequest(
				"testTitle", "testDescription", 100, Genre.FANTASY);

		Long id = getMovieId(request);

		mockMvc.perform(delete("/api/movies/" + id))
				.andExpect(status().isNoContent());

	}

	@Test
	public void deleteMovie_whenMoviesExistAndRequestIsValidAndScreeningExists_returnsBadRequest() throws Exception {
		MovieRequest request = new MovieRequest(
				"testTitle", "testDescription", 100, Genre.FANTASY);

		CinemaRoomRequest roomRequest = new CinemaRoomRequest(10, 10, 20);
		Long movieId = getMovieId(request);
		Long roomId = getCinemaId(roomRequest);

		ScreeningRequest screeningRequest = new ScreeningRequest(roomId, movieId, LocalDate.now(), TimeSlot.PRIME, BigDecimal.TEN);

		mockMvc.perform(post("/api/screening")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(screeningRequest)))
				.andExpect(status().isOk());

		mockMvc.perform(delete("/api/movies/" + movieId))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message")
						.value("Movie with id " + movieId + " cannot be deleted, screening exits"));
		;

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

	//Helper Method

	private Long getMovieId(MovieRequest movieRequest) throws Exception {
		String responseJson = mockMvc.perform(post("/api/movies")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(movieRequest)))
				.andExpect(status().isCreated())
				.andReturn().getResponse().getContentAsString();

		return ((Number) JsonPath.read(responseJson, "$.id")).longValue();
	}

	private Long getCinemaId(CinemaRoomRequest cinemaRoomRequest) throws Exception {

		String roomResponseJson = mockMvc.perform(post("/api/rooms")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(cinemaRoomRequest)))
				.andExpect(status().isCreated())
				.andReturn().getResponse().getContentAsString();

		return ((Number) JsonPath.read(roomResponseJson, "$.id")).longValue();
	}


}
