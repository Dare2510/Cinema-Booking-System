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
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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

	@MockitoBean
	private Clock clock;

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
	private static final String SCREENING_DATE = LocalDate.now().toString();
	private static final TimeSlot SCREENING_TIMESLOT = TimeSlot.PRIME;
	private static final String SCREENING_SLOT = TimeSlot.PRIME.name();

	private static final PaymentMethod RESERVATION_PAYMENT_METHOD = PaymentMethod.ONLINE;
	private static final BigDecimal RESERVATION_AMOUNT = BigDecimal.valueOf(20.0);

	@Test
	public void getPageOfReservations_withPageableDefaults_returnIsOK() throws Exception {
		mockMvc.perform(get("/api/management/reservation")
						.param("page", "0")
						.param("size", "10")
						.param("sort", "reservationStatus")
						.param("direction", "ASC"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content").isArray())
				.andExpect(jsonPath("$.size").value(10));
	}

	@Test
	public void completePayment_whenReservationStatusIsCreated_returnsIsOK() throws Exception {
		MovieRequest movie = movieRequest();
		Long movieId = createMovieAndGetId(movie);

		CinemaRoomRequest room = cinemaRoomRequest();
		Long roomId = createCinemaRoomAndGetId(room);

		ScreeningRequest screening = screeningRequest(roomId, movieId);
		Long screeningId = createScreeningAndGetId(screening);

		List<Long> seatIdsToReserve = getFreeSeatIds(screeningId).subList(0, 2);

		ReservationRequest reservation = reservationRequest(screeningId, seatIdsToReserve);
		Long reservationId = createReservationAndGetId(reservation);

		mockMvc.perform(patch("/api/management/reservation/" + reservationId + "/complete/payment"))
				.andExpect(status().isOk());

	}

	@Test
	public void completePayment_whenReservationStatusIsCancelled_returnsBadRequest() throws Exception {
		MovieRequest movie = movieRequest();
		Long movieId = createMovieAndGetId(movie);

		CinemaRoomRequest room = cinemaRoomRequest();
		Long roomId = createCinemaRoomAndGetId(room);

		ScreeningRequest screening = screeningRequest(roomId, movieId);
		Long screeningId = createScreeningAndGetId(screening);

		List<Long> seatIdsToReserve = getFreeSeatIds(screeningId).subList(0, 2);

		ReservationRequest reservation = reservationRequest(screeningId, seatIdsToReserve);
		Long reservationId = createReservationAndGetId(reservation);

		mockCurrentTime(minutesBeforeScreening(90));
		cancelReservation(reservationId);

		mockMvc.perform(patch("/api/management/reservation/" + reservationId + "/complete/payment"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value(
						"Payment for reservation with " + reservationId + " id cannot be completed." +
								" Check status of reservation and payment"));
	}

	@Test
	public void refundPayment_whenReservationStatusIsCancelled_returnsIsOk() throws Exception {
		MovieRequest movie = movieRequest();
		Long movieId = createMovieAndGetId(movie);

		CinemaRoomRequest room = cinemaRoomRequest();
		Long roomId = createCinemaRoomAndGetId(room);

		ScreeningRequest screening = screeningRequest(roomId, movieId);
		Long screeningId = createScreeningAndGetId(screening);

		List<Long> seatIdsToReserve = getFreeSeatIds(screeningId).subList(0, 2);

		ReservationRequest reservation = reservationRequest(screeningId, seatIdsToReserve);
		Long reservationId = createReservationAndGetId(reservation);

		completePayment(reservationId);
		mockCurrentTime(minutesBeforeScreening(90));
		cancelReservation(reservationId);

		mockMvc.perform(patch("/api/management/reservation/" + reservationId + "/refund"))
				.andExpect(status().isOk());
	}

	@Test
	public void refundPayment_whenReservationStatusIsCreated_returnsBadRequest() throws Exception {
		MovieRequest movie = movieRequest();
		Long movieId = createMovieAndGetId(movie);

		CinemaRoomRequest room = cinemaRoomRequest();
		Long roomId = createCinemaRoomAndGetId(room);

		ScreeningRequest screening = screeningRequest(roomId, movieId);
		Long screeningId = createScreeningAndGetId(screening);

		List<Long> seatIdsToReserve = getFreeSeatIds(screeningId).subList(0, 2);

		ReservationRequest reservation = reservationRequest(screeningId, seatIdsToReserve);
		Long reservationId = createReservationAndGetId(reservation);

		mockMvc.perform(patch("/api/management/reservation/" + reservationId + "/refund"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value(
						"Reservation with " + reservationId +
								" id cannot be refunded. Check status of reservation and payment"));
	}

	@Test
	public void setTicketToUsed_whenStatusIsValid_returnsNoContent() throws Exception {
		MovieRequest movie = movieRequest();
		Long movieId = createMovieAndGetId(movie);

		CinemaRoomRequest room = cinemaRoomRequest();
		Long roomId = createCinemaRoomAndGetId(room);

		ScreeningRequest screening = screeningRequest(roomId, movieId);
		Long screeningId = createScreeningAndGetId(screening);

		List<Long> seatIdsToReserve = getFreeSeatIds(screeningId).subList(0, 2);

		ReservationRequest reservation = reservationRequest(screeningId, seatIdsToReserve);
		Long reservationId = createReservationAndGetId(reservation);
		String ticketNumber = getTicketNumber(reservationId);
		completePayment(reservationId);

		mockMvc.perform(patch("/api/management/reservation/ticket/" + ticketNumber + "/used"))
				.andExpect(status().isNoContent());

	}

	@Test
	public void setTicketToUsed_whenStatusIsInvalid_returnsBadRequest() throws Exception {
		MovieRequest movie = movieRequest();
		Long movieId = createMovieAndGetId(movie);

		CinemaRoomRequest room = cinemaRoomRequest();
		Long roomId = createCinemaRoomAndGetId(room);

		ScreeningRequest screening = screeningRequest(roomId, movieId);
		Long screeningId = createScreeningAndGetId(screening);

		List<Long> seatIdsToReserve = getFreeSeatIds(screeningId).subList(0, 2);

		ReservationRequest reservation = reservationRequest(screeningId, seatIdsToReserve);
		Long reservationId = createReservationAndGetId(reservation);
		String ticketNumber = getTicketNumber(reservationId);

		mockMvc.perform(patch("/api/management/reservation/ticket/" + ticketNumber + "/used"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message")
						.value("Ticket number " + ticketNumber + " cannot check in, check status"));

	}

	@Test
	public void setTicketToUsed_whenIsNotFound_returnsNotFound() throws Exception {
		String ticketNumber = "123456789";

		mockMvc.perform(patch("/api/management/reservation/ticket/" + ticketNumber + "/used"))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.message")
						.value("Ticket number " + ticketNumber + " not found"));

	}

	@Test
	public void getReservationById_whenReservationExists_returnsOK() throws Exception {
		MovieRequest movie = movieRequest();
		Long movieId = createMovieAndGetId(movie);

		CinemaRoomRequest room = cinemaRoomRequest();
		Long roomId = createCinemaRoomAndGetId(room);

		ScreeningRequest screening = screeningRequest(roomId, movieId);
		Long screeningId = createScreeningAndGetId(screening);

		List<Long> seatIdsToReserve = getFreeSeatIds(screeningId).subList(0, 2);

		ReservationRequest reservation = reservationRequest(screeningId, seatIdsToReserve);
		Long reservationId = createReservationAndGetId(reservation);

		mockMvc.perform(get("/api/management/reservation/" + reservationId))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.reservationId").value(reservationId))
				.andExpect(jsonPath("$.paymentResponse.amount").value(RESERVATION_AMOUNT))
				.andExpect(jsonPath("$.timeSlot").value(SCREENING_SLOT))
				.andExpect(jsonPath("$.screeningDate").value(SCREENING_DATE))
				.andExpect(jsonPath("$.ticketNumber").exists());
	}


	@Test
	public void getReservationById_whenReservationDoesNotExists_returnsNotFound() throws Exception {
		mockMvc.perform(get("/api/management/reservation/" + 99))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.message")
						.value("Reservation with id " + 99 + " not found"));

	}

	@Test
	public void createReservation_screeningExistsAndSeatsAreFree_returnsOK() throws Exception {
		MovieRequest movie = movieRequest();
		Long movieId = createMovieAndGetId(movie);

		CinemaRoomRequest room = cinemaRoomRequest();
		Long roomId = createCinemaRoomAndGetId(room);

		ScreeningRequest screening = screeningRequest(roomId, movieId);
		Long screeningId = createScreeningAndGetId(screening);

		List<Long> seatIdsToReserve = getFreeSeatIds(screeningId).subList(0, 2);

		ReservationRequest reservation = reservationRequest(screeningId, seatIdsToReserve);

		mockMvc.perform(post("/api/management/reservation")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(reservation)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.reservedSeats.size()").value(seatIdsToReserve.size()))
				.andExpect(jsonPath("$.paymentResponse.recipient").value("Cinema-Booking System"))
				.andExpect(jsonPath("$.paymentResponse.amount").value(RESERVATION_AMOUNT))
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

		ScreeningRequest screening = screeningRequest(roomId, movieId);
		Long screeningId = createScreeningAndGetId(screening);

		List<Long> seatIdsToReserve = getFreeSeatIds(screeningId).subList(0, 2);

		ReservationRequest reservation = reservationRequest(screeningId, seatIdsToReserve);
		postReservation(reservation);

		mockMvc.perform(post("/api/management/reservation")
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

		ScreeningRequest screening = screeningRequest(roomId, movieId);
		Long screeningId = createScreeningAndGetId(screening);

		List<Long> seatIdsToReserve = getFreeSeatIds(screeningId).subList(0, 2);

		ReservationRequest reservation = reservationRequest(screeningId, seatIdsToReserve);
		Long reservationId = createReservationAndGetId(reservation);

		mockCurrentTime(minutesBeforeScreening(90));

		mockMvc.perform(patch("/api/management/reservation/" + reservationId + "/cancel"))
				.andExpect(status().isOk());
	}

	@Test
	public void cancelReservation_whenStatusIsAlreadyCanceled_returnsBadRequest() throws Exception {
		MovieRequest movie = movieRequest();
		Long movieId = createMovieAndGetId(movie);

		CinemaRoomRequest room = cinemaRoomRequest();
		Long roomId = createCinemaRoomAndGetId(room);

		ScreeningRequest screening = screeningRequest(roomId, movieId);
		Long screeningId = createScreeningAndGetId(screening);

		List<Long> seatIdsToReserve = getFreeSeatIds(screeningId).subList(0, 2);

		ReservationRequest reservation = reservationRequest(screeningId, seatIdsToReserve);
		Long reservationId = createReservationAndGetId(reservation);

		mockCurrentTime(minutesBeforeScreening(90));

		cancelReservation(reservationId);

		mockMvc.perform(patch("/api/management/reservation/" + reservationId + "/cancel"))
				.andExpect(status().isBadRequest());
	}

	@Test
	public void cancelReservation_whenCancelRequestIsToLate_returnsBadRequest() throws Exception {
		MovieRequest movie = movieRequest();
		Long movieId = createMovieAndGetId(movie);

		CinemaRoomRequest room = cinemaRoomRequest();
		Long roomId = createCinemaRoomAndGetId(room);

		ScreeningRequest screening = screeningRequest(roomId, movieId);
		Long screeningId = createScreeningAndGetId(screening);

		List<Long> seatIdsToReserve = getFreeSeatIds(screeningId).subList(0, 2);

		ReservationRequest reservation = reservationRequest(screeningId, seatIdsToReserve);
		Long reservationId = createReservationAndGetId(reservation);

		mockCurrentTime(minutesBeforeScreening(30));

		mockMvc.perform(patch("/api/management/reservation/" + reservationId + "/cancel"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").
						value("Reservation with id " + reservationId + " cannot be cancelled." +
								"Reservation must be cancelled at least 60 min before screening"));
	}


	private void completePayment(Long reservationId) throws Exception {
		mockMvc.perform(patch("/api/management/reservation/" + reservationId + "/complete/payment"))
				.andExpect(status().isOk());
	}

	private void cancelReservation(Long reservationId) throws Exception {
		mockMvc.perform(patch("/api/reservation/" + reservationId + "/cancel"))
				.andExpect(status().isOk());
	}

	private ReservationRequest reservationRequest(Long screeningId, List<Long> seatsToReserve) {
		return new ReservationRequest(screeningId, seatsToReserve, RESERVATION_PAYMENT_METHOD);
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

	private Long createReservationAndGetId(ReservationRequest reservation) throws Exception {
		String reservationJson = mockMvc.perform(post("/api/management/reservation")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(reservation)))
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();

		return ((Number) JsonPath.read(reservationJson, "$.reservationId")).longValue();
	}

	private void postReservation(ReservationRequest reservation) throws Exception {
		mockMvc.perform(post("/api/management/reservation")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(reservation)))
				.andExpect(status().isOk());
	}

	private String getTicketNumber(Long reservationId) throws Exception {
		String reservationJson = mockMvc.perform(get("/api/management/reservation/" + reservationId))
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();

		return (JsonPath.read(reservationJson, "$.ticketNumber")).toString();
	}

	private List<Long> getFreeSeatIds(Long screeningId) throws Exception {
		String jsonResponse = mockMvc.perform(get("/api/management/screening/" + screeningId + "/seats/free"))
				.andReturn().getResponse().getContentAsString();

		List<Integer> freeSeatIds = JsonPath.read(jsonResponse, "$[*].cinemaRoomSeatId");

		return freeSeatIds.stream()
				.map(Integer::longValue)
				.toList();

	}

	private LocalDateTime minutesBeforeScreening(int minutes) {
		return LocalDateTime.of(
				LocalDate.parse(SCREENING_DATE),
				SCREENING_TIMESLOT.getStartTime().minusMinutes(minutes)
		);
	}

	private void mockCurrentTime(LocalDateTime currentTime) {
		ZoneId zone = ZoneId.systemDefault();

		given(clock.instant()).willReturn(currentTime.atZone(zone).toInstant());
		given(clock.getZone()).willReturn(zone);
	}

}