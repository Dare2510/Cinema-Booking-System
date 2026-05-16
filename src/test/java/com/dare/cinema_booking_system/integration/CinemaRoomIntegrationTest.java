package com.dare.cinema_booking_system.integration;

import com.dare.cinema_booking_system.movies.dto.MovieRequest;
import com.dare.cinema_booking_system.movies.entity.Genre;
import com.dare.cinema_booking_system.rooms.dto.CinemaRoomRequest;
import com.dare.cinema_booking_system.rooms.repository.CinemaRoomRepository;
import com.dare.cinema_booking_system.rooms.repository.SeatRepository;
import com.dare.cinema_booking_system.screenings.dto.ScreeningsRequest;
import com.dare.cinema_booking_system.screenings.entity.TimeSlot;
import com.dare.cinema_booking_system.screenings.repository.ScreeningsRepository;
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
	private ScreeningsRepository screeningsRepository;

	@AfterEach
	public void tearDown() {
		cinemaRoomRepository.deleteAll();
		seatRepository.deleteAll();
		screeningsRepository.deleteAll();
	}

	@Test
	public void createRoomAndGetRoomById_whenJsonAndRoomNumberAreValid_returns200() throws Exception {
		CinemaRoomRequest request = new CinemaRoomRequest(1, 10, 20);

		String responseJson = mockMvc.perform(post("/api/rooms")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isCreated())
				.andReturn().getResponse().getContentAsString();

		int id = JsonPath.read(responseJson, "$.id");

		mockMvc.perform(get("/api/rooms/" + id))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(id))
				.andExpect(jsonPath("$.roomNumber").value(1))
				.andExpect(jsonPath("$.capacity").value(200));
	}

	@Test
	public void createRoom_whenJsonValuesAreInvalid_returnsBadRequest() throws Exception {
		CinemaRoomRequest request = new CinemaRoomRequest(1, 5, 20);

		mockMvc.perform(post("/api/rooms")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value("Minium number of rows is 10"));

	}

	@Test
	public void getRoomById_whenRoomDoesNotExist_returnNotFound() throws Exception {
		mockMvc.perform(get("/api/rooms/999"))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.message").value("Cinema Room with ID 999 not found"));
	}

	@Test
	public void createRoomAndUpdateRoom_whenJsonAndRoomNumberAreValidAndNoScreeningExists_returns200() throws Exception {
		CinemaRoomRequest request = new CinemaRoomRequest(1, 10, 20);

		String responseJson = mockMvc.perform(post("/api/rooms")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isCreated())
				.andReturn().getResponse().getContentAsString();

		int id = JsonPath.read(responseJson, "$.id");

		request.setRowCapacity(25);

		mockMvc.perform(patch("/api/rooms/update/" + id)
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(id))
				.andExpect(jsonPath("$.roomNumber").value(1))
				.andExpect(jsonPath("$.capacity").value(250));

	}

	@Test
	public void createRoomMovieScreeningUpdateRoom_whenJsonAndRoomNumberAreValidAndScreeningExists_returnsBadRequest() throws Exception {
		CinemaRoomRequest request = new CinemaRoomRequest(1, 10, 20);

		String roomResponseJson = mockMvc.perform(post("/api/rooms")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isCreated())
				.andReturn().getResponse().getContentAsString();

		Long roomId = ((Number) JsonPath.read(roomResponseJson, "$.id")).longValue();

		MovieRequest movieRequest = new MovieRequest("TestTitle", "TestDescription", 90, Genre.FANTASY);

		String movieResponseJson = mockMvc.perform(post("/api/movies")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(movieRequest)))
				.andReturn().getResponse().getContentAsString();

		Long movieId = ((Number) JsonPath.read(movieResponseJson, "$.id")).longValue();

		ScreeningsRequest screeningsRequest = new ScreeningsRequest(roomId, movieId, LocalDate.now(), TimeSlot.PRIME, BigDecimal.valueOf(5));

		mockMvc.perform(post("/api/screening")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(screeningsRequest)))
				.andExpect(status().isOk());

		request.setRowCapacity(25);

		mockMvc.perform(patch("/api/rooms/update/" + roomId)
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message")
						.value("Changes are not possible for cinema room with id " + roomId + " screening exits"));

	}

	@Test
	public void createRoomAndDeleteRoom_whenRoomExistsButNoScreeningExists_returnsNoContent() throws Exception {
		CinemaRoomRequest request = new CinemaRoomRequest(1, 10, 20);

		String responseJson = mockMvc.perform(post("/api/rooms")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isCreated())
				.andReturn().getResponse().getContentAsString();

		int id = JsonPath.read(responseJson, "$.id");

		mockMvc.perform(delete("/api/rooms/delete/" + id))
				.andExpect(status().isNoContent());
	}

	@Test
	public void getPageOfRooms_withPageableDefaults_returnIsOK() throws Exception {
		mockMvc.perform(get("/api/rooms")
						.param("page", "0")
						.param("size", "10")
						.param("sort", "roomNumber")
						.param("direction", "ASC"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content").isArray())
				.andExpect(jsonPath("$.size").value(10));

	}

}
