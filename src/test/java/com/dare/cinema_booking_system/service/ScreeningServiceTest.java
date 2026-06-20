package com.dare.cinema_booking_system.service;

import com.dare.cinema_booking_system.movie.entity.Genre;
import com.dare.cinema_booking_system.movie.entity.MovieEntity;
import com.dare.cinema_booking_system.movie.service.MovieService;
import com.dare.cinema_booking_system.rooms.entity.CinemaRoomEntity;
import com.dare.cinema_booking_system.rooms.entity.SeatEntity;
import com.dare.cinema_booking_system.rooms.service.CinemaRoomService;
import com.dare.cinema_booking_system.screenings.dto.ScreeningRequest;
import com.dare.cinema_booking_system.screenings.dto.ScreeningResponse;
import com.dare.cinema_booking_system.screenings.entity.ScreeningEntity;
import com.dare.cinema_booking_system.screenings.entity.ScreeningSeatEntity;
import com.dare.cinema_booking_system.screenings.entity.TimeSlot;
import com.dare.cinema_booking_system.screenings.exceptions.ScreeningNotFoundException;
import com.dare.cinema_booking_system.screenings.exceptions.ScreeningSlotAlreadyBookedException;
import com.dare.cinema_booking_system.screenings.exceptions.ScreeningUpdateNotPossibleException;
import com.dare.cinema_booking_system.screenings.repository.ScreeningRepository;
import com.dare.cinema_booking_system.screenings.repository.ScreeningSeatRepository;
import com.dare.cinema_booking_system.screenings.service.ScreeningSeatService;
import com.dare.cinema_booking_system.screenings.service.ScreeningService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ScreeningServiceTest {

	private static final Long SCREENING_ID = 1L;
	private static final Long ROOM_ID = 1L;
	private static final Long MOVIE_ID = 1L;

	private static final LocalDate SCREENING_DATE = LocalDate.now();
	private static final TimeSlot TIME_SLOT = TimeSlot.PRIME;
	private static final BigDecimal PRICE = BigDecimal.valueOf(10);

	@InjectMocks
	private ScreeningService screeningService;

	@Mock
	private ScreeningRepository screeningRepository;

	@Mock
	private ScreeningSeatService screeningSeatService;

	@Mock
	private ScreeningSeatRepository screeningSeatRepository;

	@Mock
	private MovieService movieService;

	@Mock
	private CinemaRoomService cinemaRoomService;

	@Test
	void getScreeningById_whenScreeningIsFound_returnsScreeningResponse() {
		MovieEntity movie = movie();
		CinemaRoomEntity room = roomWithSeats();
		ScreeningEntity screening = screening(movie, room, SCREENING_DATE, TIME_SLOT, BigDecimal.valueOf(5));

		when(screeningRepository.findById(SCREENING_ID)).thenReturn(Optional.of(screening));
		when(cinemaRoomService.getRoomEntity(ROOM_ID)).thenReturn(room);

		ScreeningResponse response = screeningService.getScreeningById(SCREENING_ID);

		assertNotNull(response);
		assertEquals(movie.getTitle(), response.getMovieInformation().getTitle());
		assertEquals(TIME_SLOT, response.getTimeSlot());
		assertEquals(BigDecimal.valueOf(5), response.getPrice());
		assertEquals(SCREENING_DATE, response.getScreeningDate());

		verify(screeningRepository).findById(SCREENING_ID);
		verify(cinemaRoomService).getRoomEntity(ROOM_ID);
	}

	@Test
	void getScreeningById_whenScreeningIsNotFound_throwsScreeningNotFoundException() {
		when(screeningRepository.findById(SCREENING_ID)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> screeningService.getScreeningById(SCREENING_ID))
				.isInstanceOf(ScreeningNotFoundException.class)
				.hasMessage("Screening with id 1 not found");

		verify(screeningRepository).findById(SCREENING_ID);
	}

	@Test
	void getScreeningEntity_whenScreeningIsFound_returnsScreeningEntity() {
		ScreeningEntity screening = screening();

		when(screeningRepository.findById(SCREENING_ID)).thenReturn(Optional.of(screening));

		ScreeningEntity result = screeningService.getScreeningEntity(SCREENING_ID);

		assertSame(screening, result);

		verify(screeningRepository).findById(SCREENING_ID);
	}

	@Test
	void getScreeningEntity_whenScreeningIsNotFound_throwsScreeningNotFoundException() {
		when(screeningRepository.findById(SCREENING_ID)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> screeningService.getScreeningEntity(SCREENING_ID))
				.isInstanceOf(ScreeningNotFoundException.class)
				.hasMessage("Screening with id 1 not found");

		verify(screeningRepository).findById(SCREENING_ID);
	}

	@Test
	void deleteScreeningById_whenScreeningIsFoundWithoutReservations_deletesScreening() {
		ScreeningEntity screening = screening();

		when(screeningSeatService.validateScreeningUpdate(SCREENING_ID)).thenReturn(false);
		when(screeningRepository.findById(SCREENING_ID)).thenReturn(Optional.of(screening));

		screeningService.deleteScreeningById(SCREENING_ID);

		verify(screeningSeatService).validateScreeningUpdate(SCREENING_ID);
		verify(screeningRepository).findById(SCREENING_ID);
		verify(screeningRepository).delete(screening);
	}

	@Test
	void deleteScreeningById_whenScreeningIsFoundWithReservations_throwsScreeningUpdateNotPossibleException() {
		when(screeningSeatService.validateScreeningUpdate(SCREENING_ID)).thenReturn(true);

		assertThatThrownBy(() -> screeningService.deleteScreeningById(SCREENING_ID))
				.isInstanceOf(ScreeningUpdateNotPossibleException.class)
				.hasMessage("Screening with id 1 cannot be updated");

		verify(screeningSeatService).validateScreeningUpdate(SCREENING_ID);
		verify(screeningRepository, never()).findById(anyLong());
		verify(screeningRepository, never()).delete(any());
	}

	@Test
	void createScreening_whenSpotIsAvailable_returnsScreeningResponse() {
		MovieEntity movie = movie();
		CinemaRoomEntity room = roomWithSeats();
		ScreeningRequest request = screeningRequest();

		mockMovieAndRoom(movie, room);
		mockSpotReserved(request, false);
		mockScreeningSaveAssignsId();
		mockCreateScreeningSeats(room);

		ScreeningResponse response = screeningService.createScreening(request);

		assertNotNull(response);
		assertEquals(PRICE, response.getPrice());
		assertEquals(SCREENING_DATE, response.getScreeningDate());
		assertEquals(TIME_SLOT, response.getTimeSlot());
		assertEquals(movie.getTitle(), response.getMovieInformation().getTitle());
		assertEquals(room.getRoomNumber(), response.getCinemaRoomInformation().getRoomNumber());

		verify(movieService).getMovieEntityById(MOVIE_ID);
		verify(cinemaRoomService, times(2)).getRoomEntity(ROOM_ID);
		verify(screeningRepository).existsByCinemaRoomIdAndScreeningDateAndTimeSlot(
				ROOM_ID,
				SCREENING_DATE,
				TIME_SLOT
		);
		verify(screeningRepository).save(any(ScreeningEntity.class));
		verify(screeningSeatService).createScreeningSeats(eq(room), any(ScreeningEntity.class));
	}

	@Test
	void createScreening_whenSpotIsUnavailable_throwsScreeningSlotAlreadyBookedException() {
		MovieEntity movie = movie();
		CinemaRoomEntity room = roomWithSeats();
		ScreeningRequest request = screeningRequest();

		mockMovieAndRoom(movie, room);
		mockSpotReserved(request, true);

		assertThatThrownBy(() -> screeningService.createScreening(request))
				.isInstanceOf(ScreeningSlotAlreadyBookedException.class)
				.hasMessage("Screening slot in 1 room ID and " + SCREENING_DATE + " and timeslot PRIME already booked");

		verify(movieService).getMovieEntityById(MOVIE_ID);
		verify(cinemaRoomService).getRoomEntity(ROOM_ID);
		verify(screeningRepository).existsByCinemaRoomIdAndScreeningDateAndTimeSlot(
				ROOM_ID,
				SCREENING_DATE,
				TIME_SLOT
		);
		verify(screeningRepository, never()).save(any());
		verify(screeningSeatService, never()).createScreeningSeats(any(), any());
	}

	@Test
	void updateScreening_whenScreeningHasReservation_throwsScreeningUpdateNotPossibleException() {
		ScreeningEntity existingScreening = screening();

		when(screeningRepository.findById(SCREENING_ID)).thenReturn(Optional.of(existingScreening));
		when(screeningSeatService.validateScreeningUpdate(SCREENING_ID)).thenReturn(true);

		assertThatThrownBy(() -> screeningService.updateScreening(SCREENING_ID, updatedScreeningRequest()))
				.isInstanceOf(ScreeningUpdateNotPossibleException.class)
				.hasMessage("Screening with id 1 cannot be updated");

		verify(screeningRepository).findById(SCREENING_ID);
		verify(screeningSeatService).validateScreeningUpdate(SCREENING_ID);

		verifyNoUpdateWrites();
	}

	@Test
	void updateScreening_whenNoReservationsAndSameSpot_updatesScreeningWithoutCheckingSlotAgain() {
		MovieEntity oldMovie = movie();
		MovieEntity updatedMovie = movie(2L, "updatedTitle", "updatedDescription", Genre.FANTASY, 100);
		CinemaRoomEntity room = roomWithSeats();
		ScreeningEntity screening = screening(oldMovie, room, SCREENING_DATE, TIME_SLOT, PRICE);
		ScreeningRequest request = new ScreeningRequest(
				ROOM_ID,
				updatedMovie.getId(),
				SCREENING_DATE,
				TIME_SLOT,
				BigDecimal.valueOf(15)
		);

		screening.getScreeningSeats().add(new ScreeningSeatEntity(screening, room.getSeats().get(0)));

		when(screeningRepository.findById(SCREENING_ID)).thenReturn(Optional.of(screening));
		when(screeningSeatService.validateScreeningUpdate(SCREENING_ID)).thenReturn(false);
		when(movieService.getMovieEntityById(updatedMovie.getId())).thenReturn(updatedMovie);
		when(cinemaRoomService.getRoomEntity(ROOM_ID)).thenReturn(room);
		when(screeningSeatService.createScreeningSeats(room, screening))
				.thenReturn(screening.getScreeningSeats());

		ScreeningResponse response = screeningService.updateScreening(SCREENING_ID, request);

		assertNotNull(response);
		assertEquals("updatedTitle", response.getMovieInformation().getTitle());
		assertEquals(BigDecimal.valueOf(15), response.getPrice());
		assertEquals(TIME_SLOT, response.getTimeSlot());
		assertEquals(SCREENING_DATE, response.getScreeningDate());

		assertEquals(updatedMovie, screening.getMovie());
		assertEquals(BigDecimal.valueOf(15), screening.getPrice());

		verify(screeningRepository).findById(SCREENING_ID);
		verify(screeningSeatService).validateScreeningUpdate(SCREENING_ID);
		verify(screeningRepository, never())
				.existsByCinemaRoomIdAndScreeningDateAndTimeSlot(anyLong(), any(), any());

		verify(screeningSeatService).createScreeningSeats(room, screening);
		verify(screeningRepository).save(screening);

		verify(cinemaRoomService, times(2)).getRoomEntity(ROOM_ID);
	}

	@Test
	void updateScreening_whenNoReservationsAndNewSpotIsAvailable_updatesScreening() {
		MovieEntity oldMovie = movie();
		MovieEntity updatedMovie = movie(2L, "updatedTitle", "updatedDescription", Genre.FANTASY, 100);

		CinemaRoomEntity oldRoom = roomWithSeats();
		CinemaRoomEntity newRoom = roomWithSeats(5L);

		ScreeningEntity screening = screening(oldMovie, oldRoom, SCREENING_DATE, TIME_SLOT, PRICE);

		ScreeningRequest request = new ScreeningRequest(
				newRoom.getId(),
				updatedMovie.getId(),
				SCREENING_DATE.plusDays(1),
				TimeSlot.EVENING,
				BigDecimal.valueOf(15)
		);

		when(screeningRepository.findById(SCREENING_ID)).thenReturn(Optional.of(screening));
		when(screeningSeatService.validateScreeningUpdate(SCREENING_ID)).thenReturn(false);
		when(screeningRepository.existsByCinemaRoomIdAndScreeningDateAndTimeSlot(
				request.getRoomId(),
				request.getScreeningDate(),
				request.getTimeSlot()
		)).thenReturn(false);

		when(movieService.getMovieEntityById(updatedMovie.getId())).thenReturn(updatedMovie);
		when(cinemaRoomService.getRoomEntity(newRoom.getId())).thenReturn(newRoom);
		mockCreateScreeningSeats(newRoom);

		ScreeningResponse response = screeningService.updateScreening(SCREENING_ID, request);

		assertNotNull(response);
		assertEquals("updatedTitle", response.getMovieInformation().getTitle());
		assertEquals(BigDecimal.valueOf(15), response.getPrice());
		assertEquals(TimeSlot.EVENING, response.getTimeSlot());
		assertEquals(SCREENING_DATE.plusDays(1), response.getScreeningDate());

		assertEquals(updatedMovie, screening.getMovie());
		assertEquals(newRoom.getId(), screening.getCinemaRoomId());
		assertEquals(TimeSlot.EVENING, screening.getTimeSlot());

		verify(screeningRepository).existsByCinemaRoomIdAndScreeningDateAndTimeSlot(
				request.getRoomId(),
				request.getScreeningDate(),
				request.getTimeSlot()
		);
		verify(screeningRepository).save(screening);
		verify(screeningSeatService).createScreeningSeats(newRoom, screening);
	}

	@Test
	void updateScreening_whenNoReservationsAndNewSpotIsBooked_throwsScreeningSlotAlreadyBookedException() {
		MovieEntity oldMovie = movie();
		CinemaRoomEntity oldRoom = roomWithSeats();
		CinemaRoomEntity requestedRoom = roomWithSeats(5L);

		ScreeningEntity screening = screening(oldMovie, oldRoom, SCREENING_DATE, TIME_SLOT, PRICE);

		ScreeningRequest request = new ScreeningRequest(
				requestedRoom.getId(),
				MOVIE_ID,
				SCREENING_DATE.plusDays(1),
				TimeSlot.EVENING,
				BigDecimal.valueOf(15)
		);

		when(screeningRepository.findById(SCREENING_ID)).thenReturn(Optional.of(screening));
		when(screeningSeatService.validateScreeningUpdate(SCREENING_ID)).thenReturn(false);
		when(screeningRepository.existsByCinemaRoomIdAndScreeningDateAndTimeSlot(
				request.getRoomId(),
				request.getScreeningDate(),
				request.getTimeSlot()
		)).thenReturn(true);
		when(cinemaRoomService.getRoomEntity(request.getRoomId())).thenReturn(requestedRoom);

		assertThatThrownBy(() -> screeningService.updateScreening(SCREENING_ID, request))
				.isInstanceOf(ScreeningSlotAlreadyBookedException.class)
				.hasMessage("Screening slot in 5 room ID and " + request.getScreeningDate() + " and timeslot EVENING already booked");

		verify(screeningRepository).findById(SCREENING_ID);
		verify(screeningSeatService).validateScreeningUpdate(SCREENING_ID);
		verify(screeningRepository).existsByCinemaRoomIdAndScreeningDateAndTimeSlot(
				request.getRoomId(),
				request.getScreeningDate(),
				request.getTimeSlot()
		);
		verify(cinemaRoomService).getRoomEntity(request.getRoomId());

		verifyNoUpdateWrites();
	}

	//Helper Methods

	//Requests

	private ScreeningRequest screeningRequest() {
		return new ScreeningRequest(ROOM_ID, MOVIE_ID, SCREENING_DATE, TIME_SLOT, PRICE);
	}

	private ScreeningRequest updatedScreeningRequest() {
		return new ScreeningRequest(
				ROOM_ID,
				MOVIE_ID,
				SCREENING_DATE.plusDays(1),
				TimeSlot.EVENING,
				BigDecimal.valueOf(15)
		);
	}

	//Entities

	private MovieEntity movie() {
		return movie(MOVIE_ID, "testTitle", "testDescription", Genre.COMEDY, 10);
	}

	private MovieEntity movie(Long id, String title, String description, Genre genre, int duration) {
		MovieEntity movie = new MovieEntity(title, description, genre, duration);
		movie.setId(id);
		return movie;
	}

	private CinemaRoomEntity roomWithSeats() {
		return roomWithSeats(ROOM_ID);
	}

	private CinemaRoomEntity roomWithSeats(Long roomId) {
		CinemaRoomEntity room = new CinemaRoomEntity(1, 10, 20, List.of(
				seat(1L, 1, 1),
				seat(2L, 1, 2)
		));
		room.setId(roomId);
		return room;
	}

	private SeatEntity seat(Long id, int rowNumber, int seatNumber) {
		SeatEntity seat = new SeatEntity();
		seat.setId(id);
		seat.setRowNumber(rowNumber);
		seat.setSeatNumber(seatNumber);
		return seat;
	}

	private ScreeningEntity screening() {
		return screening(movie(), roomWithSeats(), SCREENING_DATE, TIME_SLOT, PRICE);
	}

	private ScreeningEntity screening(
			MovieEntity movie,
			CinemaRoomEntity room,
			LocalDate date,
			TimeSlot timeSlot,
			BigDecimal price
	) {
		ScreeningEntity screening = new ScreeningEntity(room.getId(), movie, date, timeSlot, price);
		screening.setId(SCREENING_ID);
		screening.setTimes(timeSlot);
		return screening;
	}

	//Mocks

	private void mockMovieAndRoom(MovieEntity movie, CinemaRoomEntity room) {
		when(movieService.getMovieEntityById(movie.getId())).thenReturn(movie);
		when(cinemaRoomService.getRoomEntity(room.getId())).thenReturn(room);
	}

	private void mockSpotReserved(ScreeningRequest request, boolean reserved) {
		when(screeningRepository.existsByCinemaRoomIdAndScreeningDateAndTimeSlot(
				request.getRoomId(),
				request.getScreeningDate(),
				request.getTimeSlot()
		)).thenReturn(reserved);
	}

	private void mockScreeningSaveAssignsId() {
		when(screeningRepository.save(any(ScreeningEntity.class)))
				.thenAnswer(invocation -> {
					ScreeningEntity screening = invocation.getArgument(0);
					screening.setId(SCREENING_ID);
					return screening;
				});
	}

	private void mockCreateScreeningSeats(CinemaRoomEntity room) {
		when(screeningSeatService.createScreeningSeats(eq(room), any(ScreeningEntity.class)))
				.thenAnswer(invocation -> {
					ScreeningEntity screening = invocation.getArgument(1);
					List<ScreeningSeatEntity> seats = room.getSeats().stream()
							.map(seat -> new ScreeningSeatEntity(screening, seat))
							.toList();
					screening.setScreeningSeats(seats);
					return seats;
				});
	}

	//Verification

	private void verifyNoUpdateWrites() {
		verify(screeningRepository, never()).save(any());
		verify(screeningSeatRepository, never()).deleteAllInBatch(anyList());
		verify(screeningSeatRepository, never()).flush();
		verify(screeningSeatService, never()).createScreeningSeats(any(), any());
	}
}