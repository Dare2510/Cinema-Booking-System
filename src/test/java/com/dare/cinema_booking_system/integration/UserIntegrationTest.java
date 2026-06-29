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
import com.dare.cinema_booking_system.user.dto.UserPasswordValidationRequest;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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

	private static final PaymentMethod RESERVATION_PAYMENT_METHOD = PaymentMethod.ONLINE;

	private static final String USER_MAIL = "testuser@mail.com";
	private static final String USER_USERNAME = "testUser";
	private static final String USER_FIRST_NAME = "testUserFirstName";
	private static final String USER_SURNAME = "testUserSurname";

	private static final String UPDATED_USER_MAIL = "updatedtestuser@mail.com";
	private static final String UPDATED_USER_FIRST_NAME = "updateName";
	private static final String UPDATED_USER_USERNAME = "updateUser";
	private static final String UPDATED_USER_SURNAME = "updatedTestUserName";

	private static final String PASSWORD = "password";
	private static final String WRONG_PASSWORD = "Wrong password";

	private static final Role USER_ROLE = Role.USER;

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

		userRepository.deleteAll();
	}

	//Customer Tests

	@Test
	public void registerUser_whenMailIsAvailable_returnsOK() throws Exception {
		UserRequest newUser = userRequest();

		mockMvc.perform(post("/api/user/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(newUser)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.email").value(USER_MAIL))
				.andExpect(jsonPath("$.username").value(USER_USERNAME))
				.andExpect(jsonPath("$.name").value(USER_FIRST_NAME))
				.andExpect(jsonPath("$.surname").value(USER_SURNAME))
				.andExpect(jsonPath("$.role").doesNotExist());


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
						.value("User with " + USER_MAIL + " already exists"));

	}

	@Test
	public void updateUser_whenPasswordIsCorrect_returnsOK() throws Exception {
		UserRequest newUser = userRequest();

		Long userId = registerUserAndGetId(newUser);

		UsernamePasswordAuthenticationToken authentication = authenticationToken(userId);
		setAuthentication(authentication);

		UserRequest updatedUser = updateUserRequest();

		mockMvc.perform(patch("/api/user/update")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(updatedUser)))
				.andExpect(status().isOk());
	}

	@Test
	public void updateUser_whenPasswordIsIncorrect_returnsOK() throws Exception {
		UserRequest newUser = userRequest();

		Long userId = registerUserAndGetId(newUser);

		UsernamePasswordAuthenticationToken authentication = authenticationToken(userId);
		setAuthentication(authentication);

		UserRequest updatedUser = updateUserRequest();
		updatedUser.setPassword(WRONG_PASSWORD);

		mockMvc.perform(patch("/api/user/update")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(updatedUser)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value("Invalid password"));

	}

	@Test
	public void deleteUser_whenThereAreNoOpenReservations_returnsOK() throws Exception {
		UserRequest newUser = userRequest();

		Long userId = registerUserAndGetId(newUser);

		UsernamePasswordAuthenticationToken authentication = authenticationToken(userId);
		setAuthentication(authentication);

		UserPasswordValidationRequest passwordInput = new UserPasswordValidationRequest(PASSWORD);

		mockMvc.perform(delete("/api/user/delete")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(passwordInput)))
				.andExpect(status().isOk());

	}

	@Test
	public void deleteUser_whenThereAreOpenReservations_returnsBadRequest() throws Exception {
		UserRequest newUser = userRequest();

		Long userId = registerUserAndGetId(newUser);
		UsernamePasswordAuthenticationToken authentication = authenticationToken(userId);
		MovieRequest movieRequest = movieRequest();

		CinemaRoomRequest roomRequest = cinemaRoomRequest();
		Long movieId = createMovieAndGetId(movieRequest);
		Long roomId = createCinemaRoomAndGetId(roomRequest);
		ScreeningRequest screeningRequest = screeningRequest(roomId, movieId);

		Long screeningId = createScreeningAndGetId(screeningRequest);
		setAuthentication(authentication);

		List<Long> seatIdsToReserve = getFreeSeatIds(screeningId).subList(0, 2);

		setAuthentication(authentication);
		ReservationRequest reservation = reservationRequest(screeningId, seatIdsToReserve);
		postReservation(reservation);

		UserPasswordValidationRequest passwordInput = new UserPasswordValidationRequest(PASSWORD);


		mockMvc.perform(delete("/api/user/delete")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(passwordInput)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message")
						.value("Deletion not possible, you have open reservations"));

	}

	//Staff/Admin Tests

	@Test
	public void registerUserByManagement_emailIsAvailable_returnsOK() throws Exception {
		UserRequest newUser = userRequest();

		mockMvc.perform(post("/api/management/user/register/" + USER_ROLE.name())
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(newUser)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.email").value(USER_MAIL))
				.andExpect(jsonPath("$.username").value(USER_USERNAME))
				.andExpect(jsonPath("$.name").value(USER_FIRST_NAME))
				.andExpect(jsonPath("$.surname").value(USER_SURNAME))
				.andExpect(jsonPath("$.role").value(USER_ROLE.name()));
	}

	@Test
	public void deleteUserByManagement_whenThereAreNoOpenReservations_returnsOK() throws Exception {
		UserRequest newUser = userRequest();

		Long userId = registerUserAndGetId(newUser);

		mockMvc.perform(delete("/api/management/user/" + userId + "/delete"))
				.andExpect(status().isOk());
	}

	@Test
	public void updateUserByManagement_whenPasswordIsCorrect_returnsOK() throws Exception {
		UserRequest newUser = userRequest();

		Long userId = registerUserAndGetId(newUser);

		UserRequest updatedUser = updateUserRequest();

		mockMvc.perform(patch("/api/management/user/" + userId + "/" + USER_ROLE.name() + "/update")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(updatedUser)))
				.andExpect(status().isOk());
	}

	@Test
	public void getPageOfUsers_withPageableDefaults_returnIsOK() throws Exception {
		mockMvc.perform(get("/api/management/user")
						.param("page", "0")
						.param("size", "10")
						.param("sort", "surname")
						.param("direction", "ASC"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content").isArray())
				.andExpect(jsonPath("$.size").value(10));
	}

	//Helper Methods

	//Create and get IDs

	private Long createScreeningAndGetId(ScreeningRequest screeningRequest) throws Exception {
		String screeningResponseJson = mockMvc.perform(post("/api/management/screening")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(screeningRequest)))
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();

		return ((Number) JsonPath.read(screeningResponseJson, "$.id")).longValue();
	}

	private List<Long> getFreeSeatIds(Long screeningId) throws Exception {
		String jsonResponse = mockMvc.perform(get("/api/screening/" + screeningId + "/seats/free"))
				.andReturn().getResponse().getContentAsString();

		List<Integer> freeSeatIds = JsonPath.read(jsonResponse, "$[*].cinemaRoomSeatId");

		return freeSeatIds.stream()
				.map(Integer::longValue)
				.toList();
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

	private Long registerUserAndGetId(UserRequest userRequest) throws Exception {
		String userJson = mockMvc.perform(post("/api/user/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(userRequest)))
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();

		return ((Number) JsonPath.read(userJson, "$.userId")).longValue();
	}

	//Endpoint helpers

	private void postReservation(ReservationRequest reservation) throws Exception {
		mockMvc.perform(post("/api/reservation")
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

	//Requests

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
		return new UserRequest(USER_MAIL, PASSWORD, USER_USERNAME, USER_FIRST_NAME, USER_SURNAME);
	}

	private UserRequest updateUserRequest() {
		return new UserRequest(UPDATED_USER_MAIL, PASSWORD, UPDATED_USER_USERNAME, UPDATED_USER_FIRST_NAME, UPDATED_USER_SURNAME);
	}

	//User Authenticator

	public UsernamePasswordAuthenticationToken authenticationToken(Long userId) {
		AuthenticatedUser principal = new AuthenticatedUser(userId, USER_MAIL, USER_ROLE);


		return new UsernamePasswordAuthenticationToken(
				principal,
				null,
				List.of(new SimpleGrantedAuthority("ROLE_" + USER_ROLE.name())));

	}

	public void setAuthentication(UsernamePasswordAuthenticationToken authentication) {
		SecurityContextHolder.getContext().setAuthentication(authentication);
	}

}

