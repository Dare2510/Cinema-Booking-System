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

	private static final String MOVIE_TITLE = "testTitle";
	private static final String MOVIE_DESCRIPTION = "testDescription";
	private static final int MOVIE_DURATION = 100;
	private static final Genre MOVIE_GENRE = Genre.FANTASY;

	private static final String UPDATED_MOVIE_TITLE = "newTitle";

	@AfterEach
	void tearDown() {
		screeningRepository.deleteAll();
		cinemaRoomRepository.deleteAll();
		movieRepository.deleteAll();
	}

	@Test
	public void createAndGetMovie_whenJsonIsValid_returnIsCreatedAndIsOK() throws Exception {
		MovieRequest request = movieRequest();

		Long id = createMovieAndReturnId(request);

		mockMvc.perform(get("/api/management/movies/" + id))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.title").value(MOVIE_TITLE));
	}

	@Test
	public void createMovie_whenJsonIsInvalid_returnBadRequest() throws Exception {
		MovieRequest request = invalidRequest();

		mockMvc.perform(post("/api/management/movies")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value("Title is required"));
	}

	@Test
	public void getMovie_whenMovieNotExists_returnNotFound() throws Exception {
		mockMvc.perform(get("/api/management/movies/999"))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.message")
						.value("Could not find movie with id: 999"));
	}

	@Test
	public void getMovieListByGenre_whenMoviesExist_returnIsOK() throws Exception {
		MovieRequest request = movieRequest();

		postMovie(request);

		mockMvc.perform(get("/api/management/movies/filter/genre/FANTASY"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].title").value(MOVIE_TITLE));
	}

	@Test
	public void getMovieListByGenre_whenMoviesNotExist_returnNotFound() throws Exception {
		mockMvc.perform(get("/api/management/movies/filter/genre/FANTASY"))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.message")
						.value("No movies with FANTASY as genre were found"));
	}

	@Test
	public void getMovieListByDuration_whenMoviesExist_returnIsOK() throws Exception {
		MovieRequest request = movieRequest();

		postMovie(request);

		mockMvc.perform(get("/api/management/movies/filter/duration/80"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.[0].title").value(MOVIE_TITLE));
	}

	@Test
	public void getMovieListByDuration_whenMoviesNotExist_returnNotFound() throws Exception {
		mockMvc.perform(get("/api/management/movies/filter/duration/120"))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.message")
						.value("No movies with greater than 120 min duration found"));
	}

	@Test
	public void updateMovie_whenMoviesExistAndRequestIsValidAndNoScreeningExits_returnIsOK() throws Exception {
		MovieRequest request = movieRequest();

		Long id = createMovieAndReturnId(request);

		updateMovieTitle(request);

		mockMvc.perform(patch("/api/management/movies/" + id)
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.title").value(UPDATED_MOVIE_TITLE));

	}

	@Test
	public void updateMovie_whenMoviesExistAndRequestIsValidAndScreeningExits_returnsBadRequest() throws Exception {
		MovieRequest request = movieRequest();

		CinemaRoomRequest roomRequest = cinemaRoomRequest();
		Long movieId = createMovieAndReturnId(request);
		Long roomId = createCinemaRoomAndReturnId(roomRequest);

		ScreeningRequest screeningRequest = screeningRequest(roomId, movieId);

		postScreening(screeningRequest);

		updateMovieTitle(request);

		mockMvc.perform(patch("/api/management/movies/" + movieId)
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").
						value("Movie with id " + movieId + " cannot be updated, screening exits"));

	}

	@Test
	public void deleteMovie_whenMoviesExistAndRequestIsValidAndNoScreeningExists_returnNoContent() throws Exception {
		MovieRequest request = movieRequest();

		Long id = createMovieAndReturnId(request);

		mockMvc.perform(delete("/api/management/movies/" + id))
				.andExpect(status().isNoContent());

	}

	@Test
	public void deleteMovie_whenMoviesExistAndRequestIsValidAndScreeningExists_returnsBadRequest() throws Exception {
		MovieRequest request = movieRequest();

		CinemaRoomRequest roomRequest = cinemaRoomRequest();
		Long movieId = createMovieAndReturnId(request);
		Long roomId = createCinemaRoomAndReturnId(roomRequest);

		ScreeningRequest screeningRequest = screeningRequest(roomId, movieId);

		postScreening(screeningRequest);

		mockMvc.perform(delete("/api/management/movies/" + movieId))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message")
						.value("Movie with id " + movieId + " cannot be deleted, screening exits"));
		;

	}

	@Test
	public void getPageOfMovies_withPageableDefaults_returnIsOK() throws Exception {
		mockMvc.perform(get("/api/management/movies")
						.param("page", "0")
						.param("size", "10")
						.param("sort", "title")
						.param("direction", "ASC"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content").isArray())
				.andExpect(jsonPath("$.size").value(10));
	}

	//Helper Method

	private Long createMovieAndReturnId(MovieRequest movieRequest) throws Exception {
		String responseJson = mockMvc.perform(post("/api/management/movies")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(movieRequest)))
				.andExpect(status().isCreated())
				.andReturn().getResponse().getContentAsString();

		return ((Number) JsonPath.read(responseJson, "$.id")).longValue();
	}

	private Long createCinemaRoomAndReturnId(CinemaRoomRequest cinemaRoomRequest) throws Exception {

		String roomResponseJson = mockMvc.perform(post("/api/management/rooms")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(cinemaRoomRequest)))
				.andExpect(status().isCreated())
				.andReturn().getResponse().getContentAsString();

		return ((Number) JsonPath.read(roomResponseJson, "$.id")).longValue();
	}

	private MovieRequest movieRequest() {
		return new MovieRequest(MOVIE_TITLE, MOVIE_DESCRIPTION, MOVIE_DURATION, MOVIE_GENRE);
	}

	private MovieRequest invalidRequest() {
		return new MovieRequest(" ", MOVIE_DESCRIPTION, MOVIE_DURATION, MOVIE_GENRE);
	}

	private CinemaRoomRequest cinemaRoomRequest() {
		return new CinemaRoomRequest(10, 10, 20);
	}

	private void updateMovieTitle(MovieRequest movieRequest) {
		movieRequest.setTitle(UPDATED_MOVIE_TITLE);
	}

	private ScreeningRequest screeningRequest(Long roomId, Long movieId) {
		return new ScreeningRequest(roomId, movieId, LocalDate.now(), TimeSlot.PRIME, BigDecimal.TEN);
	}

	private void postScreening(ScreeningRequest screeningRequest) throws Exception {
		mockMvc.perform(post("/api/management/screening")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(screeningRequest)))
				.andExpect(status().isOk());
	}

	private void postMovie(MovieRequest movieRequest) throws Exception {
		mockMvc.perform(post("/api/management/movies")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(movieRequest)))
				.andExpect(status().isCreated());
	}


}
