package com.dare.cinema_booking_system.service;

import com.dare.cinema_booking_system.movies.entity.Genre;
import com.dare.cinema_booking_system.movies.entity.MovieEntity;
import com.dare.cinema_booking_system.movies.service.MovieService;
import com.dare.cinema_booking_system.rooms.entity.CinemaRoomEntity;
import com.dare.cinema_booking_system.rooms.entity.SeatEntity;
import com.dare.cinema_booking_system.rooms.service.CinemaRoomService;
import com.dare.cinema_booking_system.screenings.dto.ScreeningRequest;
import com.dare.cinema_booking_system.screenings.dto.ScreeningResponse;
import com.dare.cinema_booking_system.screenings.entity.ScreeningSeatEntity;
import com.dare.cinema_booking_system.screenings.entity.ScreeningEntity;
import com.dare.cinema_booking_system.screenings.entity.TimeSlot;
import com.dare.cinema_booking_system.screenings.exceptions.ScreeningNotFoundException;
import com.dare.cinema_booking_system.screenings.exceptions.ScreeningSlotAlreadyBookedException;
import com.dare.cinema_booking_system.screenings.exceptions.ScreeningUpdateNotPossibleException;
import com.dare.cinema_booking_system.screenings.repository.ScreeningSeatRepository;
import com.dare.cinema_booking_system.screenings.repository.ScreeningRepository;
import com.dare.cinema_booking_system.screenings.service.ScreeningService;
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
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
public class ScreeningServiceTest {

	@InjectMocks
	private ScreeningService screeningService;

	@Mock
	private MovieService movieService;

	@Mock
	private CinemaRoomService cinemaRoomService;

	@Mock
	private ScreeningRepository screeningRepository;

	@Mock
	private ScreeningSeatRepository screeningSeatRepository;

	@Spy
	private ModelMapper modelMapper;

	@AfterEach
	public void tearDown() {
		screeningRepository.deleteAll();
		screeningSeatRepository.deleteAll();
	}

	@Test
	public void getScreeningById_whenScreeningIsFound_returnScreeningResponse() {
		MovieEntity testMovie = new MovieEntity("testTitle", "testDescription", Genre.COMEDY, 99);
		testMovie.setId(1L);
		CinemaRoomEntity testRoom = new CinemaRoomEntity(1, 10, 20, null);
		testRoom.setId(1L);
		ScreeningEntity screening = new ScreeningEntity(testRoom.getId(), testMovie,
				LocalDate.now(), TimeSlot.PRIME, BigDecimal.valueOf(5));
		Long screeningId = 1L;
		when(cinemaRoomService.getRoomEntity(1L)).thenReturn(testRoom);
		when(screeningRepository.findById(screeningId)).thenReturn(Optional.of(screening));
		ScreeningResponse screeningResponse = screeningService.getScreeningById(screeningId);

		verify(screeningRepository, times(1)).findById(screeningId);

		assertEquals(testMovie.getTitle(), screeningResponse.getMovieInformation().title);
		assertEquals(TimeSlot.PRIME, screeningResponse.getTimeSlot());
		assertEquals(BigDecimal.valueOf(5), screeningResponse.getPrice());
	}

	@Test
	public void getScreeningById_whenScreeningIsNotFound_returnScreeningNotFoundException() {

		assertThatThrownBy(() -> screeningService.getScreeningById(1L))
				.isInstanceOf(ScreeningNotFoundException.class)
				.hasMessage("Screening with id 1 not found");
	}

	@Test
	public void deleteScreeningById_whenScreeningIsFoundWithoutReservations() {
		ScreeningEntity screeningToDelete = new ScreeningEntity();
		screeningToDelete.setId(1L);

		when(screeningSeatRepository.hasReservedOrSoldSeats(anyLong())).thenReturn(false);
		when(screeningRepository.findById(1L)).thenReturn(Optional.of(screeningToDelete));
		screeningService.deleteScreeningById(1L);

		verify(screeningSeatRepository, times(1)).hasReservedOrSoldSeats(anyLong());
		verify(screeningRepository, times(1)).delete(any());

		assertFalse(screeningRepository.existsById(1L));
	}

	@Test
	public void deleteScreeningById_whenScreeningIsFoundWithReservations_throwsScreeningUpdateNotPossibleException() {
		ScreeningEntity screeningToDelete = new ScreeningEntity();
		screeningToDelete.setId(1L);

		when(screeningSeatRepository.hasReservedOrSoldSeats(anyLong())).thenReturn(true);
		assertThatThrownBy(() -> screeningService.deleteScreeningById(1L))
				.isInstanceOf(ScreeningUpdateNotPossibleException.class)
				.hasMessage("Screening with id 1 cannot be updated");

		verify(screeningSeatRepository, times(1)).hasReservedOrSoldSeats(screeningToDelete.getId());
		verify(screeningSeatRepository, never()).findById(screeningToDelete.getId());
		verify(screeningSeatRepository, never()).delete(any());
	}

	@Test
	public void createScreening_whenSpotIsAvailable_returnScreeningResponse() {
		MovieEntity movie = new MovieEntity("testTitle", "testDescription", Genre.COMEDY, 10);
		movie.setId(1L);

		CinemaRoomEntity room = new CinemaRoomEntity(1, 10, 20, new ArrayList<>());
		room.setId(1L);

		LocalDate date = LocalDate.now();
		TimeSlot timeSlot = TimeSlot.PRIME;
		BigDecimal price = BigDecimal.valueOf(10);

		ScreeningRequest screeningRequest =
				new ScreeningRequest(room.getId(), movie.getId(), date, timeSlot, price);


		when(screeningRepository.existsByCinemaRoomIdAndScreeningDateAndTimeSlot
				(room.getId(), date, timeSlot)).thenReturn(false);
		when(movieService.getMovieEntityById(1L)).thenReturn(movie);
		when(cinemaRoomService.getRoomEntity(1L)).thenReturn(room);

		ScreeningResponse response = screeningService.createScreenings(screeningRequest);

		verify(screeningRepository, times(1)).save(any(ScreeningEntity.class));
		verify(screeningRepository, times(1)).existsByCinemaRoomIdAndScreeningDateAndTimeSlot(room.getId(), date, timeSlot);
		verify(screeningSeatRepository, times(1)).saveAll(anyList());

		assertEquals(price, response.getPrice());
		assertEquals(date, response.getScreeningDate());
		assertEquals(timeSlot, response.getTimeSlot());

	}

	@Test
	public void createScreening_whenSpotIsUnavailable_returnScreeningResponse() {
		CinemaRoomEntity room = new CinemaRoomEntity(1, 10, 20, new ArrayList<>());
		room.setId(1L);

		ScreeningRequest screeningRequest =
				new ScreeningRequest(1L, 1L, LocalDate.now(), TimeSlot.PRIME, BigDecimal.valueOf(10));
		when(cinemaRoomService.getRoomEntity(1L)).thenReturn(room);
		when(screeningRepository.existsByCinemaRoomIdAndScreeningDateAndTimeSlot
				(1L, LocalDate.now(), TimeSlot.PRIME)).thenReturn(true);

		assertThatThrownBy(() -> screeningService.createScreenings(screeningRequest))
				.isInstanceOf(ScreeningSlotAlreadyBookedException.class)
				.hasMessage("Screening slot in 1 room ID and " + LocalDate.now() + " and timeslot PRIME already booked");

		verify(screeningRepository, never()).save(any());
		verify(screeningRepository, times(1)).existsByCinemaRoomIdAndScreeningDateAndTimeSlot(screeningRequest.getRoomId(), LocalDate.now(), TimeSlot.PRIME);


	}

	@Test
	public void updateScreening_whenScreeningHasReservation_throwsScreeningUpdateNotPossibleException() {
		MovieEntity oldMovie = new MovieEntity("testTitle", "testDescription", Genre.COMEDY, 10);
		oldMovie.setId(1L);

		MovieEntity updatedMovie = new MovieEntity("updatedTitle", "updatedDescription", Genre.FANTASY, 100);
		updatedMovie.setId(2L);

		CinemaRoomEntity room = new CinemaRoomEntity(1, 10, 20, new ArrayList<>());
		room.setId(1L);

		LocalDate date = LocalDate.now();
		BigDecimal oldPrice = BigDecimal.valueOf(10);
		BigDecimal updatedPrice = BigDecimal.valueOf(15);

		ScreeningEntity screeningToUpdate =
				new ScreeningEntity(room.getId(), oldMovie, date, TimeSlot.PRIME, oldPrice);
		screeningToUpdate.setId(1L);

		ScreeningRequest request =
				new ScreeningRequest(room.getId(), updatedMovie.getId(), date, TimeSlot.EVENING, updatedPrice);

		when(screeningRepository.findById(screeningToUpdate.getId()))
				.thenReturn(Optional.of(screeningToUpdate));

		when(screeningSeatRepository.hasReservedOrSoldSeats(screeningToUpdate.getId()))
				.thenReturn(true);

		assertThatThrownBy(() -> screeningService.updateScreenings(screeningToUpdate.getId(), request))
				.isInstanceOf(ScreeningUpdateNotPossibleException.class)
				.hasMessage("Screening with id 1 cannot be updated");

		verify(screeningRepository).findById(screeningToUpdate.getId());
		verify(movieService,never()).getMovieEntityById(updatedMovie.getId());
		verify(cinemaRoomService, never()).getRoomEntity(room.getId());
		verify(screeningSeatRepository).hasReservedOrSoldSeats(screeningToUpdate.getId());

		verify(screeningRepository, never())
				.existsByCinemaRoomIdAndScreeningDateAndTimeSlot(anyLong(), any(), any());

		verify(screeningRepository, never()).save(any());
		verify(screeningSeatRepository, never()).deleteAllInBatch(anyList());
		verify(screeningSeatRepository, never()).saveAll(any());
	}

	@Test
	public void updateScreening_whenScreeningHasNoReservationAndSpotIsSameAndScreeningSeatsExist_returnScreeningResponse() {
		SeatEntity oldSeat = new SeatEntity();
		oldSeat.setId(1L);

		SeatEntity newSeat = new SeatEntity();
		newSeat.setId(2L);

		List<SeatEntity> oldSeats = new ArrayList<>();
		oldSeats.add(oldSeat);

		List<SeatEntity> newSeats = new ArrayList<>();
		newSeats.add(newSeat);

		MovieEntity oldMovie = new MovieEntity("testTitle", "testDescription", Genre.COMEDY, 10);
		oldMovie.setId(1L);

		MovieEntity updatedMovie = new MovieEntity("updatedTitle", "updatedDescription", Genre.FANTASY, 100);
		updatedMovie.setId(2L);

		CinemaRoomEntity oldRoom = new CinemaRoomEntity(1, 10, 20, oldSeats);
		oldRoom.setId(1L);

		CinemaRoomEntity newRoom = new CinemaRoomEntity(1, 10, 20, newSeats);
		newRoom.setId(5L);

		LocalDate date = LocalDate.now();
		BigDecimal oldPrice = BigDecimal.valueOf(10);
		BigDecimal updatedPrice = BigDecimal.valueOf(15);

		ScreeningEntity screeningToUpdate =
				new ScreeningEntity(oldRoom.getId(), oldMovie, date, TimeSlot.PRIME, oldPrice);
		screeningToUpdate.setId(1L);

		ScreeningSeatEntity existingScreeningSeat = new ScreeningSeatEntity();
		existingScreeningSeat.setScreening(screeningToUpdate);
		screeningToUpdate.getScreeningSeats().add(existingScreeningSeat);

		ScreeningRequest request =
				new ScreeningRequest(newRoom.getId(), updatedMovie.getId(), date, TimeSlot.PRIME, updatedPrice);

		when(screeningRepository.findById(screeningToUpdate.getId()))
				.thenReturn(Optional.of(screeningToUpdate));

		when(movieService.getMovieEntityById(updatedMovie.getId()))
				.thenReturn(updatedMovie);

		when(cinemaRoomService.getRoomEntity(newRoom.getId()))
				.thenReturn(newRoom);

		when(screeningSeatRepository.hasReservedOrSoldSeats(screeningToUpdate.getId()))
				.thenReturn(false);

		when(screeningRepository.existsByCinemaRoomIdAndScreeningDateAndTimeSlot(
				request.getRoomId(),
				request.getScreeningDate(),
				request.getTimeSlot()
		)).thenReturn(false);

		when(screeningSeatRepository.getScreeningSeatsByScreening(screeningToUpdate))
				.thenReturn(screeningToUpdate.getScreeningSeats());

		ScreeningResponse response =
				screeningService.updateScreenings(screeningToUpdate.getId(), request);

		verify(screeningRepository).findById(screeningToUpdate.getId());
		verify(movieService).getMovieEntityById(updatedMovie.getId());
		verify(cinemaRoomService,times(2)).getRoomEntity(newRoom.getId());
		verify(screeningSeatRepository).hasReservedOrSoldSeats(screeningToUpdate.getId());

		verify(screeningRepository).existsByCinemaRoomIdAndScreeningDateAndTimeSlot(
				request.getRoomId(),
				request.getScreeningDate(),
				request.getTimeSlot()
		);

		verify(screeningSeatRepository).getScreeningSeatsByScreening(screeningToUpdate);
		verify(screeningSeatRepository).deleteAllInBatch(anyList());
		verify(screeningSeatRepository).flush();
		verify(screeningSeatRepository).saveAll(argThat(seats -> seats.iterator().hasNext()));
		verify(screeningRepository).save(screeningToUpdate);

		assertEquals(updatedMovie.getTitle(), response.getMovieInformation().title);
		assertEquals(updatedPrice, response.getPrice());
		assertEquals(TimeSlot.PRIME, response.getTimeSlot());
	}


}

