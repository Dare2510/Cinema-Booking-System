package com.dare.cinema_booking_system.service;

import com.dare.cinema_booking_system.movies.entity.Genre;
import com.dare.cinema_booking_system.movies.entity.MovieEntity;
import com.dare.cinema_booking_system.movies.service.MovieService;
import com.dare.cinema_booking_system.rooms.entity.CinemaRoomEntity;
import com.dare.cinema_booking_system.rooms.service.CinemaRoomService;
import com.dare.cinema_booking_system.screenings.dto.ScreeningsRequest;
import com.dare.cinema_booking_system.screenings.dto.ScreeningsResponse;
import com.dare.cinema_booking_system.screenings.entity.ScreeningsEntity;
import com.dare.cinema_booking_system.screenings.entity.TimeSlot;
import com.dare.cinema_booking_system.screenings.exceptions.ScreeningNotFoundException;
import com.dare.cinema_booking_system.screenings.exceptions.ScreeningSlotAlreadyBookedException;
import com.dare.cinema_booking_system.screenings.exceptions.ScreeningUpdateNotPossibleException;
import com.dare.cinema_booking_system.screenings.repository.ScreeningSeatRepository;
import com.dare.cinema_booking_system.screenings.repository.ScreeningsRepository;
import com.dare.cinema_booking_system.screenings.service.ScreeningsService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
public class ScreeningsServiceTest {

	@InjectMocks
	private ScreeningsService screeningsService;

	@Mock
	private MovieService movieService;

	@Mock
	private CinemaRoomService cinemaRoomService;

	@Mock
	private ScreeningsRepository screeningsRepository;

	@Mock
	private ScreeningSeatRepository screeningSeatRepository;

	@Spy
	private ModelMapper modelMapper;

	@AfterEach
	public void tearDown() {
		screeningsRepository.deleteAll();
		screeningSeatRepository.deleteAll();
	}

	@Test
	public void getScreeningById_whenScreeningIsFound_returnScreeningResponse() {
		MovieEntity testMovie = new MovieEntity("testTitle", "testDescription", Genre.COMEDY, 99);
		testMovie.setId(1L);
		CinemaRoomEntity testRoom = new CinemaRoomEntity(1, 10, 20, null);
		testRoom.setId(1L);
		ScreeningsEntity screening = new ScreeningsEntity(testRoom.getId(), testMovie,
				LocalDate.now(), TimeSlot.PRIME, BigDecimal.valueOf(5));
		Long screeningId = 1L;

		when(screeningsRepository.findById(screeningId)).thenReturn(Optional.of(screening));
		ScreeningsResponse screeningResponse = screeningsService.getScreeningById(screeningId);

		verify(screeningsRepository, times(1)).findById(screeningId);

		assertEquals(1L, screeningResponse.getMovieId());
		assertEquals(TimeSlot.PRIME, screeningResponse.getTimeSlot());
		assertEquals(BigDecimal.valueOf(5), screeningResponse.getPrice());
	}

	@Test
	public void getScreeningById_whenScreeningIsNotFound_returnScreeningNotFoundException() {

		assertThatThrownBy(() -> screeningsService.getScreeningById(1L))
				.isInstanceOf(ScreeningNotFoundException.class)
				.hasMessage("Screening with id 1 not found");
	}

	@Test
	public void deleteScreeningById_whenScreeningIsFoundWithoutReservations() {
		ScreeningsEntity screeningToDelete = new ScreeningsEntity();
		screeningToDelete.setId(1L);

		when(screeningSeatRepository.hasReservedOrSoldSeats(anyLong())).thenReturn(false);
		when(screeningsRepository.findById(1L)).thenReturn(Optional.of(screeningToDelete));
		screeningsService.deleteScreeningById(1L);

		verify(screeningSeatRepository, times(1)).hasReservedOrSoldSeats(anyLong());
		verify(screeningsRepository, times(1)).delete(any());

		assertFalse(screeningsRepository.existsById(1L));
	}

	@Test
	public void deleteScreeningById_whenScreeningIsFoundWithReservations() {
		ScreeningsEntity screeningToDelete = new ScreeningsEntity();
		screeningToDelete.setId(1L);

		when(screeningSeatRepository.hasReservedOrSoldSeats(anyLong())).thenReturn(true);
		assertThatThrownBy(() -> screeningsService.deleteScreeningById(1L))
				.isInstanceOf(ScreeningUpdateNotPossibleException.class)
				.hasMessage("Screening with id 1 cannot be updated");
	}

	@Test
	public void createScreening_whenSpotIsAvailable_returnScreeningResponse() {
		MovieEntity movie = new MovieEntity("testTitle", "testDescription", Genre.COMEDY, 10);
		CinemaRoomEntity room = new CinemaRoomEntity(1, 10, 20, new ArrayList<>());
		LocalDate date = LocalDate.now();
		TimeSlot timeSlot = TimeSlot.PRIME;
		BigDecimal price = BigDecimal.valueOf(10);

		room.setId(1L);
		movie.setId(1L);

		ScreeningsRequest screeningsRequest =
				new ScreeningsRequest(room.getId(), movie.getId(), date, timeSlot, price);


		when(screeningsRepository.existsByCinemaRoomIdAndScreeningDateAndTimeSlot
				(room.getId(), date, timeSlot)).thenReturn(false);
		when(movieService.getMovieEntityById(1L)).thenReturn(movie);
		when(cinemaRoomService.getRoomEntity(1L)).thenReturn(room);

		ScreeningsResponse response = screeningsService.createScreenings(screeningsRequest);

		verify(screeningsRepository, times(1)).save(any(ScreeningsEntity.class));
		verify(screeningsRepository, times(1)).existsByCinemaRoomIdAndScreeningDateAndTimeSlot(room.getId(), date, timeSlot);
		verify(screeningSeatRepository, times(1)).saveAll(anyList());

		assertEquals(price, response.getPrice());
		assertEquals(date, response.getScreeningDate());
		assertEquals(timeSlot, response.getTimeSlot());

	}

	@Test
	public void createScreening_whenSpotIsUnavailable_returnScreeningResponse() {
		CinemaRoomEntity room = new CinemaRoomEntity(1, 10, 20, new ArrayList<>());
		room.setId(1L);


		ScreeningsRequest screeningsRequest =
				new ScreeningsRequest(1L, 1L, LocalDate.now(), TimeSlot.PRIME, BigDecimal.valueOf(10));
		when(cinemaRoomService.getRoomEntity(1L)).thenReturn(room);
		when(screeningsRepository.existsByCinemaRoomIdAndScreeningDateAndTimeSlot
				(1L, LocalDate.now(), TimeSlot.PRIME)).thenReturn(true);

		assertThatThrownBy(() -> screeningsService.createScreenings(screeningsRequest))
				.isInstanceOf(ScreeningSlotAlreadyBookedException.class)
				.hasMessage("Screening slot in 1 room ID and " + LocalDate.now() + " and timeslot PRIME already booked");


	}


}

