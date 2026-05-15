package com.dare.cinema_booking_system.service;

import com.dare.cinema_booking_system.rooms.dto.CinemaRoomRequest;
import com.dare.cinema_booking_system.rooms.entity.CinemaRoomEntity;
import com.dare.cinema_booking_system.rooms.entity.SeatEntity;
import com.dare.cinema_booking_system.rooms.exceptions.CinemaRoomChangesNotPossibleException;
import com.dare.cinema_booking_system.rooms.exceptions.CinemaRoomNotFoundException;
import com.dare.cinema_booking_system.rooms.exceptions.CinemaRoomNumberDuplicateException;
import com.dare.cinema_booking_system.rooms.repository.CinemaRoomRepository;
import com.dare.cinema_booking_system.rooms.repository.SeatRepository;
import com.dare.cinema_booking_system.rooms.service.CinemaRoomService;
import com.dare.cinema_booking_system.screenings.repository.ScreeningSeatRepository;
import com.dare.cinema_booking_system.screenings.repository.ScreeningsRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
public class CinemaRoomServiceTest {

	@Mock
	private CinemaRoomRepository cinemaRoomRepository;
	@Mock
	private SeatRepository seatRepository;
	@Mock
	private ScreeningsRepository screeningsRepository;
	@Spy
	private ModelMapper modelMapper;
	@InjectMocks
	private CinemaRoomService cinemaRoomService;

	@Test
	public void createCinemaRoom_whenRoomNumberIsValid_returnsCinemaResponse() {
		CinemaRoomRequest roomRequest = new CinemaRoomRequest(1, 10, 20);
		List<SeatEntity> seats = new ArrayList<>();
		CinemaRoomEntity newRoom = new CinemaRoomEntity(1, 10, 20, seats);

		when(cinemaRoomRepository.existsByRoomNumber(roomRequest.getRoomNumber())).thenReturn(false);
		when(seatRepository.saveAll(any())).thenReturn(seats);
		when(cinemaRoomRepository.save(any())).thenReturn(newRoom);

		cinemaRoomService.createCinemaRoom(roomRequest);

		assertEquals(1, newRoom.getRoomNumber());
		assertEquals(10, newRoom.getRows());
		assertEquals(20, newRoom.getRowCapacity());
		assertEquals(0, newRoom.getSeats().size());
		assertEquals(200, newRoom.getCapacity());

		verify(seatRepository, times(1)).saveAll(any());
		verify(cinemaRoomRepository, times(2)).save(any());

	}

	@Test
	public void createCinemaRoom_whenRoomNumberIsInvalid_returnsCinemaRoomDuplicateException() {
		CinemaRoomRequest roomRequest = new CinemaRoomRequest(1, 10, 20);
		when(cinemaRoomRepository.existsByRoomNumber(roomRequest.getRoomNumber())).thenReturn(true);
		assertThatThrownBy(() -> cinemaRoomService.createCinemaRoom(roomRequest))
				.isInstanceOf(CinemaRoomNumberDuplicateException.class)
				.hasMessage("Room number 1 is already in use, please choose another room number");

	}

	@Test
	public void updateCinemaRoom_whenRoomByIdDoesNotExist_returnsCinemaResponse() {
		CinemaRoomRequest roomRequest = new CinemaRoomRequest(1, 10, 20);
		assertThatThrownBy(() -> cinemaRoomService.updateCinemaRoom(roomRequest, 1L))
				.isInstanceOf(CinemaRoomNotFoundException.class)
				.hasMessage("Cinema Room with ID 1 not found");

	}

	@Test
	public void updateCinemaRoom_whenRoomByIdExistsAndNoScreeningExists_returnsCinemaResponse() {
		CinemaRoomRequest roomRequest = new CinemaRoomRequest(1, 20, 25);
		List<SeatEntity> seatsOld = new ArrayList<>();
		seatsOld.add(new SeatEntity());
		List<SeatEntity> seatsNew = new ArrayList<>();
		seatsNew.add(new SeatEntity());
		seatsNew.add(new SeatEntity());
		Long id = 1L;
		CinemaRoomEntity oldRoomEntity = new CinemaRoomEntity(1, 10, 20, seatsOld);
		CinemaRoomEntity newRoomEntity = new CinemaRoomEntity(1, 20, 25, seatsNew);

		when(cinemaRoomRepository.findById(id)).thenReturn(Optional.of(oldRoomEntity));
		when(screeningsRepository.existsByCinemaRoomId(id)).thenReturn(false);
		when(seatRepository.findByCinemaRoom(oldRoomEntity)).thenReturn(seatsOld);
		when(seatRepository.saveAll(any())).thenReturn(seatsNew);
		when(cinemaRoomRepository.save(any())).thenReturn(newRoomEntity);

		cinemaRoomService.updateCinemaRoom(roomRequest, id);

		assertEquals(1, newRoomEntity.getRoomNumber());
		assertEquals(20, newRoomEntity.getRows());
		assertEquals(25, newRoomEntity.getRowCapacity());
		assertEquals(500, newRoomEntity.getCapacity());
		assertEquals(2, newRoomEntity.getSeats().size());

		verify(cinemaRoomRepository, times(1)).findById(1L);
		verify(cinemaRoomRepository, times(2)).save(any());
		verify(seatRepository, times(1)).deleteAllInBatch(seatsOld);
		verify(seatRepository, times(1)).flush();
		verify(seatRepository, times(1)).saveAll(any());
		verify(screeningsRepository, times(1)).existsByCinemaRoomId(id);

	}

	@Test
	public void updateCinemaRoom_whenRoomByIdAndScreeningExists_returnsCinemaRoomChangesNotPossibleException() {
		CinemaRoomEntity oldRoomEntity = new CinemaRoomEntity(1, 10, 20, null);
		when(cinemaRoomRepository.findById(1L)).thenReturn(Optional.of(oldRoomEntity));
		when(screeningsRepository.existsByCinemaRoomId(1L)).thenReturn(true);

		assertThatThrownBy(() -> cinemaRoomService.updateCinemaRoom(
				new CinemaRoomRequest(1, 10, 20), 1L
		)).isInstanceOf(CinemaRoomChangesNotPossibleException.class)
				.hasMessage("Changes are not possible for cinema room with id 1 screening exits");

	}

	@Test
	public void deleteCinemaRoom_whenRoomIdDoesExists_returnsCinemaResponse() {
		when(cinemaRoomRepository.findById(any())).thenReturn(Optional.of(new CinemaRoomEntity()));
		when(screeningsRepository.existsByCinemaRoomId(1L)).thenReturn(false);
		cinemaRoomService.deleteCinemaRoom(1L);

		assertFalse(cinemaRoomRepository.existsById(1L));

		verify(cinemaRoomRepository, times(1)).findById(1L);
		verify(cinemaRoomRepository, times(1)).delete(any(CinemaRoomEntity.class));


	}

	@Test
	public void deleteCinemaRoom_whenRoomIdAndScreeningExists_returnsCinemaResponse() {
		CinemaRoomEntity oldRoomEntity = new CinemaRoomEntity(1, 10, 20, null);
		when(cinemaRoomRepository.findById(1L)).thenReturn(Optional.of(oldRoomEntity));
		when(screeningsRepository.existsByCinemaRoomId(1L)).thenReturn(true);

		assertThatThrownBy(() -> cinemaRoomService.deleteCinemaRoom(1L))
				.isInstanceOf(CinemaRoomChangesNotPossibleException.class)
				.hasMessage("Changes are not possible for cinema room with id 1 screening exits");
	}
}
