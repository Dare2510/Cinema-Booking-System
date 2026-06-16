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
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
@WithMockUser(username = "admin", roles = "ADMIN")
public class UserIntegrationTest {

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

	private static final String EMAIL = "testuser@mail.com";
	private static final String PASSWORD = "password";
	private static final String HASHED_PASSWORD = "hashedPassword";
	private static final String USERNAME = "tester";
	private static final String NAME = "testName";
	private static final String SURNAME = "testSurname";

	private static final String UPDATED_EMAIL = "newtestuser@mail.com";
	private static final String UPDATED_USERNAME = "newTester";
	private static final String UPDATED_NAME = "newTestName";
	private static final String UPDATED_SURNAME = "newTestSurname";

	private static final String WRONG_PASSWORD = "Wrong password";

	private static final Role USER_ROLE = Role.USER;
	private static final Role ADMIN_ROLE = Role.ADMIN;
	private static final Long USER_ID = 1L;

	@AfterEach
	void tearDown() {
		SecurityContextHolder.clearContext();

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
	public void registerUser_whenMailIsAvailable_returnsOK() throws Exception {
		UserRequest newUser = userRequest();

		mockMvc.perform(post("/api/user/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(newUser)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.email").value(EMAIL))
				.andExpect(jsonPath("$.username").value(USERNAME))
				.andExpect(jsonPath("$.name").value(NAME))
				.andExpect(jsonPath("$.surname").value(SURNAME));

	}

	@Test
	public void registerUser_whenMailIsNotAvailable_returnsBadRequest() throws Exception {
		UserRequest newUser = userRequest();

		postUserRegistration(newUser);

		mockMvc.perform(post("/api/user/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(newUser)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message")
						.value("User with " + EMAIL + " already exists"));

	}

	@Test
	public void updateUser_whenPasswordIsCorrect_returnsOK() throws Exception {
		UserRequest newUser = userRequest();

		Long userId = registerUserAndGetId(newUser);

		AuthenticatedUser principal = new AuthenticatedUser(userId, EMAIL, USER_ROLE);

		UsernamePasswordAuthenticationToken authentication =
				new UsernamePasswordAuthenticationToken(
						principal,
						null,
						List.of(new SimpleGrantedAuthority("ROLE_" + USER_ROLE.name()))
				);

		SecurityContextHolder.getContext().setAuthentication(authentication);

		UserRequest updatedUser = updateUserRequest();

		mockMvc.perform(patch("/api/user/" + PASSWORD + "/update")
						.with(authentication(authentication))
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(updatedUser)))
				.andExpect(status().isOk());


	}

	private void completePayment(Long reservationId) throws Exception {
		mockMvc.perform(patch("/api/management/reservation/" + reservationId + "/complete/payment"))
				.andExpect(status().isOk());
	}

	private void cancelReservation(Long reservationId) throws Exception {
		mockMvc.perform(patch("/api/management/reservation/" + reservationId + "/cancel"))
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

	private UserRequest userRequest() {
		return new UserRequest(EMAIL, PASSWORD, USERNAME, NAME, SURNAME);
	}

	private UserRequest updateUserRequest() {
		return new UserRequest(UPDATED_EMAIL, PASSWORD, UPDATED_USERNAME, UPDATED_NAME, SURNAME);
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

	private Long registerUserAndGetId(UserRequest userRequest) throws Exception {
		String userJson = mockMvc.perform(post("/api/user/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(userRequest)))
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();

		System.out.println(userJson);

		return ((Number) JsonPath.read(userJson, "$.userId")).longValue();
	}

	private void postReservation(ReservationRequest reservation) throws Exception {
		mockMvc.perform(post("/api/management/reservation")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(reservation)))
				.andExpect(status().isOk());
	}

	private void postUserRegistration(UserRequest registrationRequest) throws Exception {
		mockMvc.perform(post("/api/user/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(registrationRequest)))
				.andExpect(status().isOk());
	}

}
