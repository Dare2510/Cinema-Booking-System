package com.dare.cinema_booking_system.integration;

import com.dare.cinema_booking_system.movie.dto.MovieRequest;
import com.dare.cinema_booking_system.movie.entity.Genre;
import com.dare.cinema_booking_system.rooms.dto.CinemaRoomRequest;
import com.dare.cinema_booking_system.rooms.repository.CinemaRoomRepository;
import com.dare.cinema_booking_system.rooms.repository.SeatRepository;
import com.dare.cinema_booking_system.screenings.dto.ScreeningRequest;
import com.dare.cinema_booking_system.screenings.entity.TimeSlot;
import com.dare.cinema_booking_system.screenings.repository.ScreeningRepository;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;;
import org.springframework.http.MediaType;;
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
public class CinemaRoomIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private CinemaRoomRepository cinemaRoomRepository;

	@Autowired
	private SeatRepository seatRepository;

	@Autowired
	private ScreeningRepository screeningRepository;


	@AfterEach
	public void tearDown() {
		cinemaRoomRepository.deleteAll();
		seatRepository.deleteAll();
		screeningRepository.deleteAll();
	}

	private static final int ROOM_NUMBER = 1;
	private static final int ROWS = 10;
	private static final int ROW_CAPACITY = 20;
	private static final int ROOM_CAPACITY = 200;

	private static final int INVALID_ROW_NUMBER = 5;
	private static final int UPDATED_ROW_CAPACITY = 25;
	private static final int UPDATED_ROOM_CAPACITY = 250;

	private static final String MOVIE_TITLE = "testTitle";
	private static final String MOVIE_DESCRIPTION = "testDescription";
	private static final int MOVIE_DURATION = 90;
	private static final Genre MOVIE_GENRE = Genre.FANTASY;

	@Test
	public void createRoomAndGetRoomById_whenJsonAndRoomNumberAreValid_returns200() throws Exception {
		CinemaRoomRequest request = cinemaRoomRequest();
		Long id = createCinemaRoomAndGetId(request);

		mockMvc.perform(get("/api/management/room/" + id))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(id))
				.andExpect(jsonPath("$.roomNumber").value(ROOM_NUMBER))
				.andExpect(jsonPath("$.roomCapacity").value(ROOM_CAPACITY));
	}

	@Test
	public void createRoom_whenJsonValuesAreInvalid_returnsBadRequest() throws Exception {
		CinemaRoomRequest request = cinemaRoomRequestWithInvalidRows();

		mockMvc.perform(post("/api/management/room")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value("Minium number of rows is 10"));

	}

	@Test
	public void getRoomById_whenRoomDoesNotExist_returnNotFound() throws Exception {
		mockMvc.perform(get("/api/management/room/999"))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.message").value("Cinema Room with ID 999 not found"));
	}

	@Test
	public void createRoomAndUpdateRoom_whenJsonAndRoomNumberAreValidAndNoScreeningExists_returns200() throws Exception {
		CinemaRoomRequest request = cinemaRoomRequest();
		Long id = createCinemaRoomAndGetId(request);

		updatedCinemaRoomRequest(request);

		mockMvc.perform(patch("/api/management/room/update/" + id)
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(id))
				.andExpect(jsonPath("$.roomNumber").value(ROOM_NUMBER))
				.andExpect(jsonPath("$.roomCapacity").value(UPDATED_ROOM_CAPACITY));

	}

	@Test
	public void createRoomMovieScreeningUpdateRoom_whenJsonAndRoomNumberAreValidAndScreeningExists_returnsBadRequest() throws Exception {
		CinemaRoomRequest request = cinemaRoomRequest();

		Long roomId = createCinemaRoomAndGetId(request);

		MovieRequest movieRequest = movieRequest();
		Long movieId = createMovieAndGetID(movieRequest);

		ScreeningRequest screeningRequest = screeningRequest(roomId, movieId);

		postScreening(screeningRequest);

		updatedCinemaRoomRequest(request);

		mockMvc.perform(patch("/api/management/room/update/" + roomId)
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message")
						.value("Changes are not possible for cinema room with id " + roomId + " screening exits"));

	}

	@Test
	public void createRoomAndDeleteRoom_whenRoomExistsButNoScreeningExists_returnsNoContent() throws Exception {
		CinemaRoomRequest request = cinemaRoomRequest();
		Long id = createCinemaRoomAndGetId(request);

		mockMvc.perform(delete("/api/management/room/delete/" + id))
				.andExpect(status().isNoContent());
	}

	@Test
	public void getPageOfRooms_withPageableDefaults_returnIsOK() throws Exception {
		mockMvc.perform(get("/api/management/room")
						.param("page", "0")
						.param("size", "10")
						.param("sort", "roomNumber")
						.param("direction", "ASC"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content").isArray())
				.andExpect(jsonPath("$.size").value(10));

	}

	//Helper Method

	private Long createCinemaRoomAndGetId(CinemaRoomRequest cinemaRoomRequest) throws Exception {

		String roomResponseJson = mockMvc.perform(post("/api/management/room")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(cinemaRoomRequest)))
				.andExpect(status().isCreated())
				.andReturn().getResponse().getContentAsString();

		return ((Number) JsonPath.read(roomResponseJson, "$.id")).longValue();
	}

	private Long createMovieAndGetID(MovieRequest movieRequest) throws Exception {
		String responseJson = mockMvc.perform(post("/api/management/movie")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(movieRequest)))
				.andExpect(status().isCreated())
				.andReturn().getResponse().getContentAsString();

		return ((Number) JsonPath.read(responseJson, "$.id")).longValue();
	}

	private CinemaRoomRequest cinemaRoomRequest() {
		return new CinemaRoomRequest(ROOM_NUMBER, ROWS, ROW_CAPACITY);
	}

	private CinemaRoomRequest cinemaRoomRequestWithInvalidRows() {
		return new CinemaRoomRequest(ROOM_NUMBER, INVALID_ROW_NUMBER, ROW_CAPACITY);
	}

	private void updatedCinemaRoomRequest(CinemaRoomRequest cinemaRoomRequest) {
		cinemaRoomRequest.setRowCapacity(UPDATED_ROW_CAPACITY);
	}

	private MovieRequest movieRequest() {
		return new MovieRequest(MOVIE_TITLE, MOVIE_DESCRIPTION, MOVIE_DURATION, MOVIE_GENRE);
	}

	private ScreeningRequest screeningRequest(Long roomId, Long movieId) {
		return new ScreeningRequest(roomId, movieId, LocalDate.now(), TimeSlot.PRIME, BigDecimal.valueOf(5));
	}

	private void postScreening(ScreeningRequest screeningRequest) throws Exception {
		mockMvc.perform(post("/api/management/screening")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(screeningRequest)))
				.andExpect(status().isOk());
	}

}
