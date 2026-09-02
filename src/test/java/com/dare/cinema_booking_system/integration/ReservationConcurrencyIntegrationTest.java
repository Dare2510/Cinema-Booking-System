package com.dare.cinema_booking_system.integration;

import com.dare.cinema_booking_system.movie.dto.MovieRequest;
import com.dare.cinema_booking_system.movie.entity.Genre;
import com.dare.cinema_booking_system.movie.service.MovieService;
import com.dare.cinema_booking_system.reservations.dto.ReservationRequest;
import com.dare.cinema_booking_system.reservations.entity.PaymentMethod;
import com.dare.cinema_booking_system.reservations.repository.ReservationsRepository;
import com.dare.cinema_booking_system.reservations.service.ReservationService;
import com.dare.cinema_booking_system.rooms.dto.CinemaRoomRequest;
import com.dare.cinema_booking_system.rooms.service.CinemaRoomService;
import com.dare.cinema_booking_system.screenings.dto.ScreeningRequest;
import com.dare.cinema_booking_system.screenings.entity.ScreeningSeatStatus;
import com.dare.cinema_booking_system.screenings.entity.TimeSlot;
import com.dare.cinema_booking_system.screenings.exceptions.ScreeningSeatNotAvailableException;
import com.dare.cinema_booking_system.screenings.repository.ScreeningSeatRepository;
import com.dare.cinema_booking_system.screenings.service.ScreeningService;
import com.dare.cinema_booking_system.security.principal.AuthenticatedUser;
import com.dare.cinema_booking_system.user.dto.UserRequest;
import com.dare.cinema_booking_system.user.dto.UserResponse;
import com.dare.cinema_booking_system.user.entity.Role;
import com.dare.cinema_booking_system.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@ActiveProfiles("test-postgres")
@Testcontainers
public class ReservationConcurrencyIntegrationTest {

	@Container
	@ServiceConnection
	public static PostgreSQLContainer postgreSQLContainer =
			new PostgreSQLContainer("postgres:17-alpine");

	@Autowired
	private ReservationService reservationService;

	@Autowired
	private ScreeningService screeningService;

	@Autowired
	private MovieService movieService;

	@Autowired
	private CinemaRoomService cinemaRoomService;

	@Autowired
	private UserService userService;

	@Autowired
	private ScreeningSeatRepository screeningSeatRepository;

	@Autowired
	private ReservationsRepository reservationsRepository;

	private static final String EMAIL_FIRST_USER = "testuser@mail.com";
	private static final String EMAIL_SECOND_USER = "secondTestuser@mail.com";
	private static final String PASSWORD = "password";
	private static final String USERNAME = "tester";
	private static final String NAME = "testName";
	private static final String SURNAME = "testSurname";

	private static final Long MOVIE_ID = 1L;
	private static final String MOVIE_NAME = "testMovieName";
	private static final String MOVIE_DESCRIPTION = "testMovieDescription";
	private static final int MOVIE_DURATION = 120;
	private static final Genre MOVIE_GENRE = Genre.ACTION;

	private static final Long ROOM_ID = 1L;
	private static final int ROOM_NUMBER = 1;
	private static final int ROOM_NUMBER_OF_ROWS = 50;
	private static final int ROOM_ROW_CAPACITY = 50;

	private static final Long SCREENING_ID = 1L;
	private final LocalDate SCREENING_DATE = LocalDate.now();
	private final TimeSlot SCREENING_SLOT = TimeSlot.PRIME;
	private final BigDecimal SCREENING_PRICE = BigDecimal.valueOf(10);

	private final List<Long> SEAT_IDS = List.of(1L, 2L, 3L);

	@Test
	public void concurrentReservations_whenSameScreeningSeatsAreRequested_throwsScreeningSeatNotAvailableException()
			throws Exception {

		//Build up
		createScreeningWithBuildUp();

		AuthenticatedUser firstAuthenticatedUser = createUserAndReturnAuthenticatedUser(EMAIL_FIRST_USER);
		AuthenticatedUser secondAuthenticatedUser = createUserAndReturnAuthenticatedUser(EMAIL_SECOND_USER);

		ExecutorService executorService = Executors.newFixedThreadPool(2);

		CountDownLatch ready =
				new CountDownLatch(2);

		CountDownLatch start =
				new CountDownLatch(1);

		Callable<Boolean> firstReservation = () -> {

			ready.countDown();

			start.await();

			try {
				reservationService.createReservation(firstAuthenticatedUser, onlineReservationRequest());
				return true;
			} catch (ScreeningSeatNotAvailableException e) {
				return false;
			}
		};

		Callable<Boolean> secondReservation = () -> {

			ready.countDown();

			start.await();

			try {
				reservationService.createReservation(secondAuthenticatedUser, onlineReservationRequest());
				return true;
			} catch (ScreeningSeatNotAvailableException e) {
				return false;
			}
		};

		try {

			Future<Boolean> a = executorService.submit(firstReservation);
			Future<Boolean> b = executorService.submit(secondReservation);

			ready.await();

			start.countDown();

			boolean aSucceeded = a.get();
			boolean bSucceeded = b.get();


			long successCount =
					Stream.of(aSucceeded, bSucceeded)
							.filter(Boolean::booleanValue)
							.count();

			int numberOfReservedSeats = screeningSeatRepository.countAllByScreeningSeatStatus(ScreeningSeatStatus.RESERVED);

			assertEquals(1, successCount);
			assertEquals(3, numberOfReservedSeats);
			assertEquals(1, reservationsRepository.count());

		} finally {
			executorService.shutdown();

		}

	}

	private void createScreeningWithBuildUp() {

		MovieRequest movie = new MovieRequest(MOVIE_NAME, MOVIE_DESCRIPTION, MOVIE_DURATION, MOVIE_GENRE);
		movieService.addMovie(movie);

		CinemaRoomRequest roomRequest = new CinemaRoomRequest(ROOM_NUMBER, ROOM_NUMBER_OF_ROWS, ROOM_ROW_CAPACITY);
		cinemaRoomService.createCinemaRoom(roomRequest);

		ScreeningRequest screening = new ScreeningRequest(ROOM_ID, MOVIE_ID, SCREENING_DATE, SCREENING_SLOT, SCREENING_PRICE);
		screeningService.createScreening(screening);
	}

	private ReservationRequest onlineReservationRequest() {
		return new ReservationRequest(SCREENING_ID, SEAT_IDS, PaymentMethod.ONLINE);
	}

	private AuthenticatedUser createUserAndReturnAuthenticatedUser(String email) {
		UserRequest userRequest = new UserRequest(email, PASSWORD, USERNAME, NAME, SURNAME);
		UserResponse response = userService.registerUserByCustomer(userRequest);

		return new AuthenticatedUser(response.getUserId(), email, Role.USER);
	}

}
