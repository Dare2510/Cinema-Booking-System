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
import com.dare.cinema_booking_system.security.principal.AuthenticatedUser;
import com.dare.cinema_booking_system.user.dto.UserRequest;
import com.dare.cinema_booking_system.user.entity.Role;
import com.dare.cinema_booking_system.user.repository.UserRepository;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
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

	@Autowired
	private UserRepository userRepository;

	@MockitoBean
	private Clock clock;

	private static final PaymentMethod RESERVATION_PAYMENT_METHOD = PaymentMethod.ONLINE;
	private static final BigDecimal RESERVATION_AMOUNT = BigDecimal.valueOf(20.0);
	private static final String RESERVATION_SCREENING_DATE = LocalDate.now().toString();
	private static final TimeSlot RESERVATION_TIME_SLOT = TimeSlot.PRIME;


	@AfterEach
	void tearDown() {
		//	SecurityContextHolder.clearContext();

		reservationsRepository.deleteAll();
		paymentRepository.deleteAll();
		ticketRepository.deleteAll();

		screeningSeatRepository.deleteAll();
		screeningRepository.deleteAll();

		cinemaRoomRepository.deleteAll();
		seatRepository.deleteAll();

		movieRepository.deleteAll();

		userRepository.deleteAll();
	}

	@Test
	public void getPageOfReservations_withPageableDefaults_returnIsOK() throws Exception {
		mockMvc.perform(get("/api/management/reservation")
						.with(adminAuth())
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

		Long userId = registerUserAndGetId();

		List<Long> seatIdsToReserve = getFreeSeatIds(screeningId, userId).subList(0, 2);


		ReservationRequest reservation = reservationRequest(screeningId, seatIdsToReserve);
		Long reservationId = createReservationAndGetId(reservation, userId);

		mockMvc.perform(patch("/api/management/reservation/" + reservationId + "/complete/payment")
						.with(adminAuth()))
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

		Long userId = registerUserAndGetId();

		List<Long> seatIdsToReserve = getFreeSeatIds(screeningId, userId).subList(0, 2);
		ReservationRequest reservation = reservationRequest(screeningId, seatIdsToReserve);
		Long reservationId = createReservationAndGetId(reservation, userId);

		mockCurrentTime(minutesBeforeScreening(90));
		cancelReservation(reservationId, userId);

		mockMvc.perform(patch("/api/management/reservation/" + reservationId + "/complete/payment")
						.with(adminAuth()))
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

		Long userId = registerUserAndGetId();

		List<Long> seatIdsToReserve = getFreeSeatIds(screeningId, userId).subList(0, 2);

		ReservationRequest reservation = reservationRequest(screeningId, seatIdsToReserve);
		Long reservationId = createReservationAndGetId(reservation, userId);

		completePayment(reservationId);
		mockCurrentTime(minutesBeforeScreening(90));
		cancelReservation(reservationId, userId);

		mockMvc.perform(patch("/api/management/reservation/" + reservationId + "/refund")
						.with(adminAuth()))
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

		Long userId = registerUserAndGetId();

		List<Long> seatIdsToReserve = getFreeSeatIds(screeningId, userId).subList(0, 2);

		ReservationRequest reservation = reservationRequest(screeningId, seatIdsToReserve);
		Long reservationId = createReservationAndGetId(reservation, userId);

		mockMvc.perform(patch("/api/management/reservation/" + reservationId + "/refund")
						.with(adminAuth()))
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

		Long userId = registerUserAndGetId();

		List<Long> seatIdsToReserve = getFreeSeatIds(screeningId, userId).subList(0, 2);

		ReservationRequest reservation = reservationRequest(screeningId, seatIdsToReserve);
		Long reservationId = createReservationAndGetId(reservation, userId);
		String ticketNumber = getTicketNumber(reservationId);

		completePayment(reservationId);

		mockMvc.perform(patch("/api/management/reservation/ticket/" + ticketNumber + "/used")
						.with(adminAuth()))
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

		Long userId = registerUserAndGetId();

		List<Long> seatIdsToReserve = getFreeSeatIds(screeningId, userId).subList(0, 2);

		ReservationRequest reservation = reservationRequest(screeningId, seatIdsToReserve);
		Long reservationId = createReservationAndGetId(reservation, userId);
		String ticketNumber = getTicketNumber(reservationId);

		mockMvc.perform(patch("/api/management/reservation/ticket/" + ticketNumber + "/used")
						.with(adminAuth()))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message")
						.value("Ticket number " + ticketNumber + " cannot check in, check status"));
	}

	@Test
	public void setTicketToUsed_whenIsNotFound_returnsNotFound() throws Exception {
		String ticketNumber = "123456789";

		mockMvc.perform(patch("/api/management/reservation/ticket/" + ticketNumber + "/used")
						.with(adminAuth()))
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

		Long userId = registerUserAndGetId();

		List<Long> seatIdsToReserve = getFreeSeatIds(screeningId, userId).subList(0, 2);

		ReservationRequest reservation = reservationRequest(screeningId, seatIdsToReserve);
		Long reservationId = createReservationAndGetId(reservation, userId);

		mockMvc.perform(get("/api/management/reservation/" + reservationId)
						.with(adminAuth()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.reservationId").value(reservationId))
				.andExpect(jsonPath("$.paymentResponse.amount").value(RESERVATION_AMOUNT))
				.andExpect(jsonPath("$.timeSlot").value(RESERVATION_TIME_SLOT.name()))
				.andExpect(jsonPath("$.screeningDate").value(RESERVATION_SCREENING_DATE))
				.andExpect(jsonPath("$.ticketNumber").exists());
	}

	@Test
	public void getReservationById_whenReservationDoesNotExists_returnsNotFound() throws Exception {
		mockMvc.perform(get("/api/management/reservation/" + 99)
						.with(adminAuth()))
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

		Long userId = registerUserAndGetId();

		List<Long> seatIdsToReserve = getFreeSeatIds(screeningId, userId).subList(0, 2);

		ReservationRequest reservation = reservationRequest(screeningId, seatIdsToReserve);

		mockMvc.perform(post("/api/reservation")
						.with(userAuth(userId))
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(reservation)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.reservedSeats.size()").value(seatIdsToReserve.size()))
				.andExpect(jsonPath("$.paymentResponse.recipient").value("Cinema-Booking System"))
				.andExpect(jsonPath("$.paymentResponse.amount").value(RESERVATION_AMOUNT))
				.andExpect(result -> {
					String response = result.getResponse().getContentAsString();

					String ticketNumber = JsonPath.read(response, "$.ticketNumber");
					String paymentReference = JsonPath.read(response, "$.paymentResponse.paymentReference");

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

		Long userId = registerUserAndGetId();

		List<Long> seatIdsToReserve = getFreeSeatIds(screeningId, userId).subList(0, 2);

		ReservationRequest reservation = reservationRequest(screeningId, seatIdsToReserve);
		postReservation(reservation, userId);

		mockMvc.perform(post("/api/reservation")
						.with(userAuth(userId))
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

		Long userId = registerUserAndGetId();

		List<Long> seatIdsToReserve = getFreeSeatIds(screeningId, userId).subList(0, 2);

		ReservationRequest reservation = reservationRequest(screeningId, seatIdsToReserve);
		Long reservationId = createReservationAndGetId(reservation, userId);

		mockCurrentTime(minutesBeforeScreening(90));

		mockMvc.perform(patch("/api/reservation/" + reservationId + "/cancel")
						.with(userAuth(userId)))
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

		Long userId = registerUserAndGetId();

		List<Long> seatIdsToReserve = getFreeSeatIds(screeningId, userId).subList(0, 2);

		ReservationRequest reservation = reservationRequest(screeningId, seatIdsToReserve);
		Long reservationId = createReservationAndGetId(reservation, userId);

		mockCurrentTime(minutesBeforeScreening(90));

		cancelReservation(reservationId, userId);

		mockMvc.perform(patch("/api/reservation/" + reservationId + "/cancel")
						.with(userAuth(userId)))
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

		Long userId = registerUserAndGetId();

		List<Long> seatIdsToReserve = getFreeSeatIds(screeningId, userId).subList(0, 2);

		ReservationRequest reservation = reservationRequest(screeningId, seatIdsToReserve);
		Long reservationId = createReservationAndGetId(reservation, userId);

		mockCurrentTime(minutesBeforeScreening(30));

		mockMvc.perform(patch("/api/reservation/" + reservationId + "/cancel")
						.with(userAuth(userId)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message")
						.value("Reservation with id " + reservationId + " cannot be cancelled." +
								"Reservation must be cancelled at least 60 min before screening"));
	}

	//Helper Methods

	//Endpoint helpers

	private void completePayment(Long reservationId) throws Exception {
		mockMvc.perform(patch("/api/management/reservation/" + reservationId + "/complete/payment")
						.with(adminAuth()))
				.andExpect(status().isOk());
	}

	private void cancelReservation(Long reservationId, Long userId) throws Exception {
		mockMvc.perform(patch("/api/reservation/" + reservationId + "/cancel")
						.with(userAuth(userId)))
				.andExpect(status().isOk());
	}

	private ReservationRequest reservationRequest(Long screeningId, List<Long> seatsToReserve) {
		return new ReservationRequest(screeningId, seatsToReserve, RESERVATION_PAYMENT_METHOD);
	}

	private void postReservation(ReservationRequest reservation, Long userId) throws Exception {
		mockMvc.perform(post("/api/reservation")
						.with(userAuth(userId))
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(reservation)))
				.andExpect(status().isOk());
	}

	//Create and get IDs

	private Long createScreeningAndGetId(ScreeningRequest screeningRequest) throws Exception {
		String screeningResponseJson = mockMvc.perform(post("/api/management/screening")
						.with(adminAuth())
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(screeningRequest)))
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();

		return ((Number) JsonPath.read(screeningResponseJson, "$.id")).longValue();
	}

	private Long createMovieAndGetId(MovieRequest movieRequest) throws Exception {
		String movieResponseJson = mockMvc.perform(post("/api/management/movie")
						.with(adminAuth())
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(movieRequest)))
				.andExpect(status().isCreated())
				.andReturn().getResponse().getContentAsString();

		return ((Number) JsonPath.read(movieResponseJson, "$.id")).longValue();
	}

	private Long createCinemaRoomAndGetId(CinemaRoomRequest cinemaRoomRequest) throws Exception {
		String roomResponseJson = mockMvc.perform(post("/api/management/room")
						.with(adminAuth())
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(cinemaRoomRequest)))
				.andExpect(status().isCreated())
				.andReturn().getResponse().getContentAsString();

		return ((Number) JsonPath.read(roomResponseJson, "$.id")).longValue();
	}

	private Long createReservationAndGetId(ReservationRequest reservation, Long userId) throws Exception {
		String reservationJson = mockMvc.perform(post("/api/reservation")
						.with(userAuth(userId))
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(reservation)))
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();

		return ((Number) JsonPath.read(reservationJson, "$.reservationId")).longValue();
	}

	private Long registerUserAndGetId() throws Exception {
		UserRequest request = new UserRequest(
				"testuser@mail.com",
				"password",
				"testUser",
				"testUserFirstName",
				"testUserLastName");
		String userJson = mockMvc.perform(post("/api/user/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();

		return ((Number) JsonPath.read(userJson, "$.userId")).longValue();
	}

	private String getTicketNumber(Long reservationId) throws Exception {
		String reservationJson = mockMvc.perform(get("/api/management/reservation/" + reservationId)
						.with(adminAuth()))
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();

		return JsonPath.read(reservationJson, "$.ticketNumber").toString();
	}

	private List<Long> getFreeSeatIds(Long screeningId, Long userId) throws Exception {
		String jsonResponse = mockMvc.perform(get("/api/screening/" + screeningId + "/seats/free")
						.with(userAuth(userId)))
				.andReturn().getResponse().getContentAsString();

		List<Integer> freeSeatIds = JsonPath.read(jsonResponse, "$[*].cinemaRoomSeatId");

		return freeSeatIds.stream()
				.map(Integer::longValue)
				.toList();
	}

	//Requests

	private MovieRequest movieRequest() {
		return new MovieRequest("testTitle", "testDescription", 120, Genre.FANTASY);
	}

	private CinemaRoomRequest cinemaRoomRequest() {
		return new CinemaRoomRequest(5, 10, 20);
	}

	private ScreeningRequest screeningRequest(Long roomId, Long movieId) {
		return new ScreeningRequest(roomId, movieId, LocalDate.now(), TimeSlot.PRIME, BigDecimal.valueOf(10.00));
	}

	//Validator Helpers

	private LocalDateTime minutesBeforeScreening(int minutes) {
		return LocalDateTime.of(
				LocalDate.parse(RESERVATION_SCREENING_DATE),
				RESERVATION_TIME_SLOT.getStartTime().minusMinutes(minutes)
		);
	}

	private void mockCurrentTime(LocalDateTime currentTime) {
		ZoneId zone = ZoneId.systemDefault();

		given(clock.instant()).willReturn(currentTime.atZone(zone).toInstant());
		given(clock.getZone()).willReturn(zone);
	}

	//User Authenticator

	public RequestPostProcessor userAuth(Long userId) {
		AuthenticatedUser principal =
				new AuthenticatedUser(userId,
						"testuser@mail.com",
						Role.USER);


		UsernamePasswordAuthenticationToken auth =
				new UsernamePasswordAuthenticationToken(
						principal,
						null,
						List.of(new SimpleGrantedAuthority("ROLE_" + Role.USER.name())));

		return authentication(auth);

	}

	//Admin Authenticator

	private RequestPostProcessor adminAuth() {
		AuthenticatedUser principal = new AuthenticatedUser(
				999L,
				"admin@example.com",
				Role.ADMIN
		);

		UsernamePasswordAuthenticationToken auth =
				new UsernamePasswordAuthenticationToken(
						principal,
						null,
						List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
				);

		return authentication(auth);
	}
}