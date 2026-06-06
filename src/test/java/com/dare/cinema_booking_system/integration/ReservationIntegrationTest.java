package com.dare.cinema_booking_system.integration;

import com.dare.cinema_booking_system.movie.dto.MovieRequest;
import com.dare.cinema_booking_system.movie.entity.Genre;
import com.dare.cinema_booking_system.movie.repository.MovieRepository;
import com.dare.cinema_booking_system.reservations.dto.ReservationRequest;
import com.dare.cinema_booking_system.reservations.entity.PaymentMethod;
import com.dare.cinema_booking_system.reservations.repository.PaymentRepository;
import com.dare.cinema_booking_system.reservations.repository.ReservationsRepository;
import com.dare.cinema_booking_system.reservations.repository.TicketRepository;
import com.dare.cinema_booking_system.rooms.dto.CinemaRoomRequest;
import com.dare.cinema_booking_system.rooms.repository.CinemaRoomRepository;
import com.dare.cinema_booking_system.rooms.repository.SeatRepository;
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
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class ReservationIntegrationTest {

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

	@Autowired
	private ReservationsRepository reservationsRepository;

	@Autowired
	private TicketRepository ticketRepository;

	@Autowired
	private PaymentRepository paymentRepository;

	@Autowired
	private MovieRepository movieRepository;

	@Autowired
	private SeatRepository seatRepository;

	@AfterEach
	void tearDown() {
		reservationsRepository.deleteAll();

		paymentRepository.deleteAll();
		ticketRepository.deleteAll();

		screeningSeatRepository.deleteAll();
		screeningRepository.deleteAll();


		cinemaRoomRepository.deleteAll();
		seatRepository.deleteAll();

		movieRepository.deleteAll();


	}

	private static final String MOVIE_TITLE = "testTitle";
	private static final String MOVIE_DESCRIPTION = "testDescription";
	private static final int MOVIE_DURATION = 120;

	private static final int ROOM_NUMBER = 5;
	private static final int ROWS = 10;
	private static final int ROW_CAPACITY = 20;

	private static final BigDecimal SCREENING_PRICE = BigDecimal.valueOf(10.00);

	private static final List<Long> SEAT_IDS_TO_RESERVE = List.of(1L, 2L);
	private static final List<String> SEATS_RESPONSE = List.of("Row: 1 - Seat: 1", "Row: 1 - Seat: 2");
	private static final PaymentMethod RESERVATION_PAYMENT_METHOD = PaymentMethod.ONLINE;

	@Test
	public void createReservation_screeningExistsAndSeatsAreFree_returnsOK() throws Exception {
		MovieRequest movie = movieRequest();
		Long movieId = createMovieAndGetId(movie);

		CinemaRoomRequest room = cinemaRoomRequest();
		Long roomId = createCinemaRoomAndGetId(room);

		ScreeningRequest screening = screeningRequest(movieId, roomId);
		Long screeningId = createScreeningAndGetId(screening);

		ReservationRequest reservation = reservationRequest(screeningId);

		mockMvc.perform(post("/api/reservation/")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(reservation)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.reservedSeats.size()").value(SEAT_IDS_TO_RESERVE.size()))
				.andExpect(jsonPath("$.reservedSeats[0]").value(SEATS_RESPONSE.get(0)))
				.andExpect(jsonPath("$.reservedSeats[1]").value(SEATS_RESPONSE.get(1)))
				.andExpect(jsonPath("$.paymentResponse.recipient").value("Cinema-Booking System"))
				.andExpect(jsonPath("$.paymentResponse.amount").value(BigDecimal.valueOf(20.0)))
				.andExpect(result -> {

					result.getResponse().getContentAsString();

					String ticketNumber = JsonPath.read(result.getResponse().getContentAsString(), "$.ticketNumber");
					String paymentReference = JsonPath.read(result.getResponse().getContentAsString(),
							"$.paymentResponse.paymentReference");

					assertEquals(ticketNumber, paymentReference);
				});


	}

	@Test
	public void createReservation_screeningExistsAndSeatsAreReserved_returnsBadRequest() throws Exception {
		MovieRequest movie = movieRequest();
		Long movieId = createMovieAndGetId(movie);

		CinemaRoomRequest room = cinemaRoomRequest();
		Long roomId = createCinemaRoomAndGetId(room);

		ScreeningRequest screening = screeningRequest(movieId, roomId);
		Long screeningId = createScreeningAndGetId(screening);

		ReservationRequest reservation = reservationRequest(screeningId);
		postReservation(reservation);

		mockMvc.perform(post("/api/reservation/")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(reservation)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value("Chosen seats are not available"));
	}

	@Test
	public void cancelReservation_whenStatusIsValid_returnsOK() throws Exception {
		MovieRequest movie = movieRequest();
		Long movieId = createMovieAndGetId(movie);

		CinemaRoomRequest room = cinemaRoomRequest();
		Long roomId = createCinemaRoomAndGetId(room);

		ScreeningRequest screening = screeningRequest(movieId, roomId);
		Long screeningId = createScreeningAndGetId(screening);

		ReservationRequest reservation = reservationRequest(screeningId);
		Long reservationId = createReservationAndGetId(reservation);

		mockMvc.perform(patch("/api/reservation/"+reservationId+"/cancel"))
				.andExpect(status().isOk());
	}

	@Test
	public void cancelReservation_whenStatusIsAlreadyCanceled_returnsBadRequest() throws Exception {
		MovieRequest movie = movieRequest();
		Long movieId = createMovieAndGetId(movie);

		CinemaRoomRequest room = cinemaRoomRequest();
		Long roomId = createCinemaRoomAndGetId(room);

		ScreeningRequest screening = screeningRequest(movieId, roomId);
		Long screeningId = createScreeningAndGetId(screening);

		ReservationRequest reservation = reservationRequest(screeningId);
		Long reservationId = createReservationAndGetId(reservation);

		cancelReservation(reservationId);

		mockMvc.perform(patch("/api/reservation/"+reservationId+"/cancel"))
				.andExpect(status().isBadRequest());
	}



	private void cancelReservation(Long reservationId) throws Exception {
		mockMvc.perform(patch("/api/reservation/"+reservationId+"/cancel"))
				.andExpect(status().isOk());
	}

	private ReservationRequest reservationRequest(Long screeningId) throws Exception {
		return new ReservationRequest(screeningId, SEAT_IDS_TO_RESERVE, RESERVATION_PAYMENT_METHOD);
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

	private Long createScreeningAndGetId(ScreeningRequest screeningRequest) throws Exception {

		String screeningResponseJson = mockMvc.perform(post("/api/screening")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(screeningRequest)))
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();

		return ((Number) JsonPath.read(screeningResponseJson, "$.id")).longValue();
	}

	private Long createMovieAndGetId(MovieRequest movieRequest) throws Exception {

		String movieResponseJson = mockMvc.perform(post("/api/movies")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(movieRequest)))
				.andExpect(status().isCreated())
				.andReturn().getResponse().getContentAsString();

		return ((Number) JsonPath.read(movieResponseJson, "$.id")).longValue();
	}

	private Long createCinemaRoomAndGetId(CinemaRoomRequest cinemaRoomRequest) throws Exception {

		String roomResponseJson = mockMvc.perform(post("/api/rooms")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(cinemaRoomRequest)))
				.andExpect(status().isCreated())
				.andReturn().getResponse().getContentAsString();

		return ((Number) JsonPath.read(roomResponseJson, "$.id")).longValue();
	}

	private void postReservation(ReservationRequest reservation) throws Exception {
		mockMvc.perform(post("/api/reservation/")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(reservation)))
				.andExpect(status().isOk());
	}

	private Long createReservationAndGetId(ReservationRequest reservation) throws Exception {
		String reservationJson =  mockMvc.perform(post("/api/reservation/")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(reservation)))
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();

		return ((Number) JsonPath.read(reservationJson, "$.reservationId")).longValue();
	}
}
