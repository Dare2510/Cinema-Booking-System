package com.dare.cinema_booking_system.integration;

import com.dare.cinema_booking_system.movie.dto.MovieRequest;
import com.dare.cinema_booking_system.movie.entity.Genre;
import com.dare.cinema_booking_system.rooms.dto.CinemaRoomRequest;
import com.dare.cinema_booking_system.rooms.repository.CinemaRoomRepository;
import com.dare.cinema_booking_system.screenings.dto.ScreeningRequest;
import com.dare.cinema_booking_system.screenings.entity.TimeSlot;
import com.dare.cinema_booking_system.screenings.repository.ScreeningRepository;
import com.dare.cinema_booking_system.screenings.repository.ScreeningSeatRepository;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
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
@AutoConfigureMockMvc(addFilters = false)
@WithMockUser(username = "admin", roles = "ADMIN")
public class ScreeningIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private ScreeningRepository screeningRepository;

	@Autowired
	private ScreeningSeatRepository screeningSeatRepository;

	@Autowired
	private CinemaRoomRepository cinemaRoomRepository;


	@AfterEach
	public void cleanUp() {
		screeningSeatRepository.deleteAll();
		cinemaRoomRepository.deleteAll();
		screeningRepository.deleteAll();
	}

	private static final String MOVIE_TITLE = "testTitle";
	private static final String MOVIE_DESCRIPTION = "testDescription";
	private static final int MOVIE_DURATION = 120;
	private static final String MOVIE_GENRE = Genre.FANTASY.name();

	private static final int ROOM_NUMBER = 5;
	private static final int ROWS = 10;
	private static final int ROW_CAPACITY = 20;
	private static final int ROOM_CAPACITY = 200;

	private static final String SCREENING_DATE = LocalDate.now().toString();
	private static final String SCREENING_SLOT = TimeSlot.PRIME.name();
	private static final BigDecimal SCREENING_PRICE = BigDecimal.valueOf(10.00);
	private static final LocalDate INVALID_SCREENING_DATE = LocalDate.now().minusDays(1);

	private static final BigDecimal UPDATED_PRICE = BigDecimal.valueOf(15.00);
	private static final String UPDATED_SCREENING_SLOT = TimeSlot.EVENING.name();

	@Test
	public void createScreening_whenMovieRoomAndScreeningAreValid_returns200() throws Exception {
		MovieRequest movie = movieRequest();
		CinemaRoomRequest room = cinemaRoomRequest();


		Long movieId = createMovieAndGetId(movie);
		Long roomId = createCinemaRoomAndGetId(room);

		ScreeningRequest screening = screeningRequest(roomId, movieId);

		mockMvc.perform(post("/api/management/screening")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(screening)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.screeningDate").value(SCREENING_DATE))
				.andExpect(jsonPath("$.price").value(SCREENING_PRICE))
				.andExpect(jsonPath("$.movieInformation.title").value(MOVIE_TITLE))
				.andExpect(jsonPath("$.movieInformation.description").value(MOVIE_DESCRIPTION))
				.andExpect(jsonPath("$.movieInformation.duration").value(MOVIE_DURATION))
				.andExpect(jsonPath("$.movieInformation.genre").value(MOVIE_GENRE))
				.andExpect(jsonPath("$.cinemaRoomInformation.roomNumber").value(ROOM_NUMBER))
				.andExpect(jsonPath("$.cinemaRoomInformation.roomCapacity").value(ROOM_CAPACITY))
				.andExpect(jsonPath("$.timeSlot").value(SCREENING_SLOT));
	}

	@Test
	public void createScreening_whenMovieDoesNotExist_returnsIsNotFound() throws Exception {
		CinemaRoomRequest room = cinemaRoomRequest();
		Long roomId = createCinemaRoomAndGetId(room);

		ScreeningRequest screening = screeningRequest(roomId, 99L);

		mockMvc.perform(post("/api/management/screening")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(screening)))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.message")
						.value("Could not find movie with id: 99"));
	}

	@Test
	public void createScreening_whenRoomDoesNotExist_returnsIsNotFound() throws Exception {
		MovieRequest movie = movieRequest();
		Long movieId = createMovieAndGetId(movie);

		ScreeningRequest screening = screeningRequest(9L, movieId);

		mockMvc.perform(post("/api/management/screening")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(screening)))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.message").value("Cinema Room with ID 9 not found"));
	}

	@Test
	public void createScreening_whenJsonValueIsInvalid_returnsBadRequest() throws Exception {

		ScreeningRequest screening = screeningRequestWithInvalidDate();

		mockMvc.perform(post("/api/management/screening")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(screening)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value("Screening date must be in the present or future"));
	}

	@Test
	public void getPageOfScreenings_withPageableDefaults_returnIsOK() throws Exception {
		mockMvc.perform(get("/api/management/screening")
						.param("page", "0")
						.param("size", "10")
						.param("sort", "screeningDate")
						.param("direction", "ASC"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content").isArray())
				.andExpect(jsonPath("$.size").value(10));
	}

	@Test
	public void getScreeningById_whenScreeningDoesNotExist_returnsNotFound() throws Exception {
		mockMvc.perform(get("/api/management/screening/" + 1000))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.message").value("Screening with id " + 1000 + " not found"));

	}

	@Test
	public void getScreeningById_whenScreeningDoesExist_returnsIsOK() throws Exception {
		MovieRequest movie = movieRequest();
		CinemaRoomRequest room = cinemaRoomRequest();

		Long movieId = createMovieAndGetId(movie);
		Long roomId = createCinemaRoomAndGetId(room);

		ScreeningRequest screening = screeningRequest(roomId, movieId);
		Long screeningId = createScreeningAndGetId(screening);

		mockMvc.perform(get("/api/management/screening/" + screeningId))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(screeningId))
				.andExpect(jsonPath("$.screeningDate").value(SCREENING_DATE))
				.andExpect(jsonPath("$.price").value(SCREENING_PRICE.doubleValue()))
				.andExpect(jsonPath("$.movieInformation.title").value(MOVIE_TITLE))
				.andExpect(jsonPath("$.movieInformation.description").value(MOVIE_DESCRIPTION))
				.andExpect(jsonPath("$.movieInformation.duration").value(MOVIE_DURATION))
				.andExpect(jsonPath("$.movieInformation.genre").value(MOVIE_GENRE))
				.andExpect(jsonPath("$.cinemaRoomInformation.roomNumber").value(ROOM_NUMBER))
				.andExpect(jsonPath("$.cinemaRoomInformation.roomCapacity").value(ROOM_CAPACITY))
				.andExpect(jsonPath("$.timeSlot").value(SCREENING_SLOT));
	}

	@Test
	public void deleteScreeningById_whenScreeningHasNoReservations_returnsNoContent() throws Exception {
		MovieRequest movie = movieRequest();
		CinemaRoomRequest room = cinemaRoomRequest();

		Long movieId = createMovieAndGetId(movie);
		Long roomId = createCinemaRoomAndGetId(room);

		ScreeningRequest screening = screeningRequest(roomId, movieId);
		Long screeningId = createScreeningAndGetId(screening);

		mockMvc.perform(delete("/api/management/screening/" + screeningId))
				.andExpect(status().isNoContent());
	}

	@Test
	public void updateScreeningById_whenScreeningHasNoReservations_returnsOK() throws Exception {
		MovieRequest movie = movieRequest();
		CinemaRoomRequest room = cinemaRoomRequest();

		Long movieId = createMovieAndGetId(movie);
		Long roomId = createCinemaRoomAndGetId(room);

		ScreeningRequest screening = screeningRequest(roomId, movieId);

		Long screeningId = createScreeningAndGetId(screening);
		ScreeningRequest update = uodatedScreeningRequest(roomId, movieId);

		mockMvc.perform(patch("/api/management/screening/" + screeningId)
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(update)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(screeningId))
				.andExpect(jsonPath("$.screeningDate").value(SCREENING_DATE))
				.andExpect(jsonPath("$.price").value(UPDATED_PRICE.doubleValue()))
				.andExpect(jsonPath("$.movieInformation.title").value(MOVIE_TITLE))
				.andExpect(jsonPath("$.movieInformation.description").value(MOVIE_DESCRIPTION))
				.andExpect(jsonPath("$.movieInformation.duration").value(MOVIE_DURATION))
				.andExpect(jsonPath("$.movieInformation.genre").value(MOVIE_GENRE))
				.andExpect(jsonPath("$.cinemaRoomInformation.roomNumber").value(ROOM_NUMBER))
				.andExpect(jsonPath("$.cinemaRoomInformation.roomCapacity").value(ROOM_CAPACITY))
				.andExpect(jsonPath("$.timeSlot").value(UPDATED_SCREENING_SLOT));
	}

	//Helper Methods

	private Long createScreeningAndGetId(ScreeningRequest screeningRequest) throws Exception {

		String screeningResponseJson = mockMvc.perform(post("/api/management/screening")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(screeningRequest)))
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();

		return ((Number) JsonPath.read(screeningResponseJson, "$.id")).longValue();
	}

	private Long createMovieAndGetId(MovieRequest movieRequest) throws Exception {

		String movieResponseJson = mockMvc.perform(post("/api/management/movie")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(movieRequest)))
				.andExpect(status().isCreated())
				.andReturn().getResponse().getContentAsString();

		return ((Number) JsonPath.read(movieResponseJson, "$.id")).longValue();
	}

	private Long createCinemaRoomAndGetId(CinemaRoomRequest cinemaRoomRequest) throws Exception {

		String roomResponseJson = mockMvc.perform(post("/api/management/room")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(cinemaRoomRequest)))
				.andExpect(status().isCreated())
				.andReturn().getResponse().getContentAsString();

		return ((Number) JsonPath.read(roomResponseJson, "$.id")).longValue();
	}

	private MovieRequest movieRequest() {
		return new MovieRequest(MOVIE_TITLE, MOVIE_DESCRIPTION, MOVIE_DURATION, Genre.FANTASY);
	}

	private CinemaRoomRequest cinemaRoomRequest() {
		return new CinemaRoomRequest(ROOM_NUMBER, ROWS, ROW_CAPACITY);
	}

	private ScreeningRequest screeningRequest(Long roomId, Long movieId) {
		return new ScreeningRequest(roomId, movieId, LocalDate.now(), TimeSlot.PRIME, SCREENING_PRICE);

	}

	private ScreeningRequest uodatedScreeningRequest(Long roomId, Long movieId) {
		return new ScreeningRequest(roomId, movieId, LocalDate.now(), TimeSlot.EVENING, UPDATED_PRICE);

	}

	private ScreeningRequest screeningRequestWithInvalidDate() {
		return new ScreeningRequest(99L, 50L, INVALID_SCREENING_DATE, TimeSlot.PRIME, SCREENING_PRICE);

	}


}
