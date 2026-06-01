package com.dare.cinema_booking_system.integration;

import com.dare.cinema_booking_system.movies.dto.MovieRequest;
import com.dare.cinema_booking_system.movies.entity.Genre;
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

	@Test
	public void createScreening_whenMovieRoomAndScreeningAreValid_returns200() throws Exception {
		MovieRequest movie = new MovieRequest("testTitle", "testDescription", 120, Genre.FANTASY);
		CinemaRoomRequest room = new CinemaRoomRequest(5, 10, 20);


		Long movieId = getMovieId(movie);
		Long roomId = getCinemaId(room);

		ScreeningRequest screening = new ScreeningRequest(roomId, movieId, LocalDate.now(), TimeSlot.PRIME, BigDecimal.TEN);

		mockMvc.perform(post("/api/screening")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(screening)))
				.andExpect(status().isOk())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.screeningDate").value(String.valueOf(LocalDate.now())))
				.andExpect(jsonPath("$.price").value(10.00))
				.andExpect(jsonPath("$.movieInformation.title").value("testTitle"))
				.andExpect(jsonPath("$.movieInformation.description").value("testDescription"))
				.andExpect(jsonPath("$.movieInformation.duration").value(120))
				.andExpect(jsonPath("$.movieInformation.genre").value("FANTASY"))
				.andExpect(jsonPath("$.cinemaRoomInformation.roomNumber").value(room.getRoomNumber()))
				.andExpect(jsonPath("$.cinemaRoomInformation.capacity").value(200))
				.andExpect(jsonPath("$.timeSlot").value("PRIME"));
	}

	@Test
	public void createScreening_whenMovieDoesNotExist_returnsIsNotFound() throws Exception {
		CinemaRoomRequest room = new CinemaRoomRequest(5, 10, 20);
		Long roomId = getCinemaId(room);

		ScreeningRequest screening = new ScreeningRequest(roomId, 99L, LocalDate.now(), TimeSlot.PRIME, BigDecimal.TEN);

		mockMvc.perform(post("/api/screening")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(screening)))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.message")
						.value("Could not find movie with id: 99"));
	}

	@Test
	public void createScreening_whenRoomDoesNotExist_returnsIsNotFound() throws Exception {
		MovieRequest movie = new MovieRequest("testTitle", "testDescription", 120, Genre.FANTASY);
		Long movieId = getMovieId(movie);

		ScreeningRequest screening = new ScreeningRequest(9L, movieId, LocalDate.now(), TimeSlot.PRIME, BigDecimal.TEN);

		mockMvc.perform(post("/api/screening")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(screening)))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.message").value("Cinema Room with ID 9 not found"));
	}

	@Test
	public void createScreening_whenJsonValueIsInvalid_returnsBadRequest() throws Exception {

		ScreeningRequest screening = new ScreeningRequest
				(50L, 51L, LocalDate.of(2020, 10, 25), TimeSlot.PRIME, BigDecimal.TEN);

		mockMvc.perform(post("/api/screening")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(screening)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value("Screening date must be in the present or future"));
	}

	@Test
	public void getPageOfScreenings_withPageableDefaults_returnIsOK() throws Exception {
		mockMvc.perform(get("/api/screening")
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
		mockMvc.perform(get("/api/screening/" + 1000))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.message").value("Screening with id " + 1000 + " not found"));

	}

	@Test
	public void getScreeningById_whenScreeningDoesExist_returnsIsOK() throws Exception {
		MovieRequest movie = new MovieRequest("testTitle", "testDescription", 120, Genre.FANTASY);
		CinemaRoomRequest room = new CinemaRoomRequest(5, 10, 20);

		Long movieId = getMovieId(movie);
		Long roomId = getCinemaId(room);

		ScreeningRequest screening = new ScreeningRequest(roomId, movieId, LocalDate.now(), TimeSlot.PRIME, BigDecimal.valueOf(15));
		Long screeningId = getScreeningId(screening);

		mockMvc.perform(get("/api/screening/" + screeningId))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(screeningId))
				.andExpect(jsonPath("$.screeningDate").value(String.valueOf(LocalDate.now())))
				.andExpect(jsonPath("$.price").value(15.00))
				.andExpect(jsonPath("$.movieInformation.title").value("testTitle"))
				.andExpect(jsonPath("$.movieInformation.description").value("testDescription"))
				.andExpect(jsonPath("$.movieInformation.duration").value(120))
				.andExpect(jsonPath("$.movieInformation.genre").value("FANTASY"))
				.andExpect(jsonPath("$.cinemaRoomInformation.roomNumber").value(room.getRoomNumber()))
				.andExpect(jsonPath("$.cinemaRoomInformation.capacity").value(200))
				.andExpect(jsonPath("$.timeSlot").value("PRIME"));
	}

	@Test
	public void deleteScreeningById_whenScreeningHasNoReservations_returnsNoContent() throws Exception {
		MovieRequest movie = new MovieRequest("testTitle", "testDescription", 120, Genre.FANTASY);
		CinemaRoomRequest room = new CinemaRoomRequest(5, 10, 20);

		Long movieId = getMovieId(movie);
		Long roomId = getCinemaId(room);

		ScreeningRequest screening = new ScreeningRequest(roomId, movieId, LocalDate.now(), TimeSlot.PRIME, BigDecimal.TEN);
		Long screeningId = getScreeningId(screening);

		mockMvc.perform(delete("/api/screening/" + screeningId))
				.andExpect(status().isNoContent());
	}

	@Test
	public void updateScreeningById_whenScreeningHasNoReservations_returnsOK() throws Exception {
		MovieRequest movie = new MovieRequest("testTitle", "testDescription", 120, Genre.FANTASY);
		CinemaRoomRequest room = new CinemaRoomRequest(5, 10, 20);

		Long movieId = getMovieId(movie);
		Long roomId = getCinemaId(room);

		ScreeningRequest screening = new ScreeningRequest(roomId, movieId, LocalDate.now(), TimeSlot.PRIME, BigDecimal.TEN);

		Long screeningId = getScreeningId(screening);
		ScreeningRequest update = new ScreeningRequest(roomId, movieId, LocalDate.now(), TimeSlot.EVENING, BigDecimal.valueOf(15));

		mockMvc.perform(patch("/api/screening/" + screeningId)
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(update)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(screeningId))
				.andExpect(jsonPath("$.screeningDate").value(String.valueOf(LocalDate.now())))
				.andExpect(jsonPath("$.price").value(15.00))
				.andExpect(jsonPath("$.movieInformation.title").value("testTitle"))
				.andExpect(jsonPath("$.movieInformation.description").value("testDescription"))
				.andExpect(jsonPath("$.movieInformation.duration").value(120))
				.andExpect(jsonPath("$.movieInformation.genre").value("FANTASY"))
				.andExpect(jsonPath("$.cinemaRoomInformation.roomNumber").value(room.getRoomNumber()))
				.andExpect(jsonPath("$.cinemaRoomInformation.capacity").value(200))
				.andExpect(jsonPath("$.timeSlot").value("EVENING"));
	}

	//Helper Methods

	private Long getScreeningId(ScreeningRequest screeningRequest) throws Exception {

		String screeningResponseJson = mockMvc.perform(post("/api/screening")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(screeningRequest)))
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();

		return ((Number) JsonPath.read(screeningResponseJson, "$.id")).longValue();
	}

	private Long getMovieId(MovieRequest movieRequest) throws Exception {

		String movieResponseJson = mockMvc.perform(post("/api/movies")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(movieRequest)))
				.andExpect(status().isCreated())
				.andReturn().getResponse().getContentAsString();

		return ((Number) JsonPath.read(movieResponseJson, "$.id")).longValue();
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
