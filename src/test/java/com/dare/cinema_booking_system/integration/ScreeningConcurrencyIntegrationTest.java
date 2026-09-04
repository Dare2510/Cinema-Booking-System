package com.dare.cinema_booking_system.integration;

import com.dare.cinema_booking_system.movie.dto.MovieRequest;
import com.dare.cinema_booking_system.movie.dto.MovieResponse;
import com.dare.cinema_booking_system.movie.entity.Genre;
import com.dare.cinema_booking_system.movie.repository.MovieRepository;
import com.dare.cinema_booking_system.movie.service.MovieService;
import com.dare.cinema_booking_system.rooms.dto.CinemaRoomRequest;
import com.dare.cinema_booking_system.rooms.dto.CinemaRoomResponse;
import com.dare.cinema_booking_system.rooms.repository.CinemaRoomRepository;
import com.dare.cinema_booking_system.rooms.service.CinemaRoomService;
import com.dare.cinema_booking_system.screenings.dto.ScreeningRequest;
import com.dare.cinema_booking_system.screenings.dto.ScreeningResponse;
import com.dare.cinema_booking_system.screenings.entity.ScreeningEntity;
import com.dare.cinema_booking_system.screenings.entity.TimeSlot;
import com.dare.cinema_booking_system.screenings.exceptions.ScreeningSlotAlreadyBookedException;
import com.dare.cinema_booking_system.screenings.repository.ScreeningRepository;
import com.dare.cinema_booking_system.screenings.repository.ScreeningSeatRepository;
import com.dare.cinema_booking_system.screenings.service.ScreeningService;
import org.junit.jupiter.api.AfterEach;
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
public class ScreeningConcurrencyIntegrationTest {

	@Container
	@ServiceConnection
	public static PostgreSQLContainer postgreSQLContainer =
			new PostgreSQLContainer("postgres:17-alpine");

	@Autowired
	private MovieService movieService;

	@Autowired
	private CinemaRoomService cinemaRoomService;

	@Autowired
	private ScreeningService screeningService;

	@Autowired
	private MovieRepository movieRepository;

	@Autowired
	private CinemaRoomRepository cinemaRoomRepository;

	@Autowired
	private ScreeningSeatRepository  screeningSeatRepository;

	@Autowired
	private ScreeningRepository screeningRepository;


	private static final String MOVIE_TITLE = "testTitle";
	private static final String MOVIE_DESCRIPTION = "testDescription";
	private static final int MOVIE_DURATION = 120;
	private static final Genre MOVIE_GENRE = Genre.FANTASY;

	private static final int ROOM_NUMBER = 1;
	private static final int ROWS = 10;
	private static final int ROW_CAPACITY = 20;

	private static final LocalDate SCREENING_DATE = LocalDate.now();
	private static final LocalDate SECOND_SCREENING_DATE = LocalDate.now().plusDays(1);
	private static final LocalDate CONCURRENT_SCREENING_DATE = LocalDate.now().plusDays(2);
	private static final TimeSlot SCREENING_SLOT = TimeSlot.PRIME;
	private static final BigDecimal SCREENING_PRICE = BigDecimal.valueOf(10.00);

	@AfterEach
	public void tearDown() {
		screeningSeatRepository.deleteAll();
		screeningRepository.deleteAll();
		cinemaRoomRepository.deleteAll();
		movieRepository.deleteAll();
	}


	@Test
	public void concurrentScreeningCreation_sameSpot_throwsScreeningSlotAlreadyBookedException() throws Exception {
		//Build up

		MovieRequest movieRequest = new MovieRequest(MOVIE_TITLE, MOVIE_DESCRIPTION, MOVIE_DURATION, MOVIE_GENRE);

		MovieResponse movieResponse = movieService.addMovie(movieRequest);

		CinemaRoomRequest roomRequest = new CinemaRoomRequest(ROOM_NUMBER, ROWS, ROW_CAPACITY);
		CinemaRoomResponse roomResponse = cinemaRoomService.createCinemaRoom(roomRequest);


		ScreeningRequest screeningRequest = new ScreeningRequest(roomResponse.getId(), movieResponse.getId(), SCREENING_DATE, SCREENING_SLOT, SCREENING_PRICE);

		ExecutorService executorService = Executors.newFixedThreadPool(2);


		CountDownLatch ready = new CountDownLatch(2);

		CountDownLatch start = new CountDownLatch(1);

		Callable<Boolean> createScreening = () -> {

			try {

				ready.countDown();

				start.await();

				screeningService.createScreening(screeningRequest);

				return true;

			} catch (ScreeningSlotAlreadyBookedException e) {

				return false;
			}

		};

		try {

			Future<Boolean> screeningA = executorService.submit(createScreening);
			Future<Boolean> screeningB = executorService.submit(createScreening);

			ready.await();
			start.countDown();

			boolean aSuccess = screeningA.get();
			boolean bSuccess = screeningB.get();

			long successSum =
					Stream.of(aSuccess, bSuccess)
							.filter(Boolean::booleanValue)
							.count();

			assertEquals(1, successSum);
			assertEquals(1, screeningRepository.count());
		} catch (ExecutionException e) {
			throw new RuntimeException(e);
		} finally {
			executorService.shutdown();

		}

	}

	@Test
	public void concurrentScreeningUpdate_sameSpot_throwsScreeningSlotAlreadyBookedException() throws Exception {

		MovieRequest movieRequest = new MovieRequest(MOVIE_TITLE, MOVIE_DESCRIPTION, MOVIE_DURATION, MOVIE_GENRE);

		MovieResponse movieResponse = movieService.addMovie(movieRequest);

		CinemaRoomRequest roomRequest = new CinemaRoomRequest(ROOM_NUMBER, ROWS, ROW_CAPACITY);
		CinemaRoomResponse roomResponse = cinemaRoomService.createCinemaRoom(roomRequest);

		ScreeningRequest firstScreening = new ScreeningRequest(roomResponse.getId(),movieResponse.getId(), SCREENING_DATE, SCREENING_SLOT, SCREENING_PRICE);
		ScreeningRequest secondScreening = new ScreeningRequest(roomResponse.getId(),movieResponse.getId(), SECOND_SCREENING_DATE, SCREENING_SLOT, SCREENING_PRICE);

		ScreeningResponse firstScreeningResponse = screeningService.createScreening(firstScreening);
		ScreeningResponse secondScreeningResponse = screeningService.createScreening(secondScreening);

		ScreeningRequest concurrentScreeningUpdate = new ScreeningRequest(roomResponse.getId(),movieResponse.getId(),CONCURRENT_SCREENING_DATE, SCREENING_SLOT, SCREENING_PRICE);

		ExecutorService executorService = Executors.newFixedThreadPool(2);

		CountDownLatch ready = new CountDownLatch(2);
		CountDownLatch start = new CountDownLatch(1);

		Callable<Boolean> updateFirstScreening = () -> {
			try {
				ready.countDown();
				start.await();

				screeningService.updateScreening(firstScreeningResponse.getId(),concurrentScreeningUpdate);
				return true;

			} catch (ScreeningSlotAlreadyBookedException e) {

				return false;
			}
		};

		Callable<Boolean> updateSecondScreening = () -> {
			try {
				ready.countDown();
				start.await();

				screeningService.updateScreening(secondScreeningResponse.getId(),concurrentScreeningUpdate);
				return true;

			} catch (ScreeningSlotAlreadyBookedException e) {

				return false;
			}
		};

		start.countDown();

		try {

			executorService.submit(updateFirstScreening);
			executorService.submit(updateSecondScreening);

			boolean aSuccess = executorService.submit(updateFirstScreening).get();
			boolean bSuccess = executorService.submit(updateSecondScreening).get();

			List<ScreeningEntity> successFullUpdates = screeningRepository.screeningsByDateAndTimeSlot(CONCURRENT_SCREENING_DATE,SCREENING_SLOT);

			long success = Stream.of(aSuccess,bSuccess)
					.filter(Boolean :: valueOf)
					.count();

			assertEquals(1, success);
			assertEquals(1, successFullUpdates.size());
		} finally {
			executorService.shutdown();
		}



	}

}
