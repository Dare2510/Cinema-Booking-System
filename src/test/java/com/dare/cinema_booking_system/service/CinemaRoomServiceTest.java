package com.dare.cinema_booking_system.service;

import com.dare.cinema_booking_system.rooms.dto.CinemaRoomRequest;
import com.dare.cinema_booking_system.rooms.dto.CinemaRoomResponse;
import com.dare.cinema_booking_system.rooms.entity.CinemaRoomEntity;
import com.dare.cinema_booking_system.rooms.entity.SeatEntity;
import com.dare.cinema_booking_system.rooms.exceptions.CinemaRoomChangesNotPossibleException;
import com.dare.cinema_booking_system.rooms.exceptions.CinemaRoomNotFoundException;
import com.dare.cinema_booking_system.rooms.exceptions.CinemaRoomNumberDuplicateException;
import com.dare.cinema_booking_system.rooms.repository.CinemaRoomRepository;
import com.dare.cinema_booking_system.rooms.repository.SeatRepository;
import com.dare.cinema_booking_system.rooms.service.CinemaRoomService;
import com.dare.cinema_booking_system.screenings.repository.ScreeningRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CinemaRoomServiceTest {

	private static final Long ROOM_ID = 1L;

	private static final int ROOM_NUMBER = 1;
	private static final int ROWS = 10;
	private static final int ROW_CAPACITY = 20;
	private static final int ROOM_CAPACITY = 200;

	private static final int UPDATED_ROWS = 20;
	private static final int UPDATED_ROW_CAPACITY = 25;
	private static final int UPDATED_ROOM_CAPACITY = 500;

	@Mock
	private CinemaRoomRepository cinemaRoomRepository;

	@Mock
	private SeatRepository seatRepository;

	@Mock
	private ScreeningRepository screeningRepository;

	@Spy
	private ModelMapper modelMapper;

	@InjectMocks
	private CinemaRoomService cinemaRoomService;

	@Test
	void createCinemaRoom_whenRoomNumberIsValid_returnsCinemaRoomResponse() {
		CinemaRoomRequest request = roomRequest();

		when(cinemaRoomRepository.existsByRoomNumber(ROOM_NUMBER)).thenReturn(false);
		mockRoomSaveAssignsId();
		mockSaveAllSeats();

		CinemaRoomResponse response = cinemaRoomService.createCinemaRoom(request);

		assertEquals(ROOM_ID, response.getId());
		assertEquals(ROOM_NUMBER, response.getRoomNumber());
		assertEquals(ROOM_CAPACITY, response.getRoomCapacity());

		verify(cinemaRoomRepository).existsByRoomNumber(ROOM_NUMBER);
		verify(seatRepository, times(1)).saveAll(anyList());
		verify(cinemaRoomRepository, times(2)).save(any(CinemaRoomEntity.class));

	}

	@Test
	void createCinemaRoom_whenRoomNumberAlreadyExists_throwsCinemaRoomNumberDuplicateException() {
		CinemaRoomRequest request = roomRequest();

		when(cinemaRoomRepository.existsByRoomNumber(ROOM_NUMBER)).thenReturn(true);

		assertThatThrownBy(() -> cinemaRoomService.createCinemaRoom(request))
				.isInstanceOf(CinemaRoomNumberDuplicateException.class)
				.hasMessage("Room number 1 is already in use, please choose another room number");

		verify(cinemaRoomRepository).existsByRoomNumber(ROOM_NUMBER);
		verify(cinemaRoomRepository, never()).save(any());
		verify(seatRepository, never()).saveAll(anyList());

	}

	@Test
	void updateCinemaRoom_whenRoomByIdDoesNotExist_throwsCinemaRoomNotFoundException() {
		CinemaRoomRequest request = updatedRoomRequest();

		when(cinemaRoomRepository.findById(ROOM_ID)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> cinemaRoomService.updateCinemaRoom(request, ROOM_ID))
				.isInstanceOf(CinemaRoomNotFoundException.class)
				.hasMessage("Cinema Room with ID 1 not found");

		verify(cinemaRoomRepository).findById(ROOM_ID);
		verify(cinemaRoomRepository, never()).save(any());
		verify(seatRepository, never()).saveAll(anyList());
	}

	@Test
	void updateCinemaRoom_whenRoomExistsAndNoScreeningExists_returnsCinemaRoomResponse() {
		CinemaRoomRequest request = updatedRoomRequest();
		CinemaRoomEntity room = roomEntityWithOneExistingSeat();

		when(cinemaRoomRepository.findById(ROOM_ID)).thenReturn(Optional.of(room));
		when(screeningRepository.existsByCinemaRoomId(ROOM_ID)).thenReturn(false);
		when(seatRepository.findByCinemaRoom(room)).thenReturn(new ArrayList<>(room.getSeats()));
		mockRoomSaveReturnsSameEntity();
		mockSaveAllSeats();

		CinemaRoomResponse response = cinemaRoomService.updateCinemaRoom(request, ROOM_ID);

		assertNotNull(response);
		assertEquals(ROOM_ID, response.getId());
		assertEquals(ROOM_NUMBER, response.getRoomNumber());
		assertEquals(UPDATED_ROOM_CAPACITY, response.getRoomCapacity());

		assertEquals(ROOM_NUMBER, room.getRoomNumber());
		assertEquals(UPDATED_ROWS, room.getRows());
		assertEquals(UPDATED_ROW_CAPACITY, room.getRowCapacity());
		assertEquals(UPDATED_ROOM_CAPACITY, room.getRoomCapacity());
		assertEquals(UPDATED_ROOM_CAPACITY, room.getSeats().size());

		verify(cinemaRoomRepository).findById(ROOM_ID);
		verify(screeningRepository).existsByCinemaRoomId(ROOM_ID);
		verify(seatRepository).findByCinemaRoom(room);
		verify(seatRepository).deleteAllInBatch(anyList());
		verify(seatRepository).flush();
		verify(seatRepository).saveAll(room.getSeats());
		verify(cinemaRoomRepository, times(2)).save(room);
	}

	@Test
	void updateCinemaRoom_whenRoomExistsAndScreeningExists_throwsCinemaRoomChangesNotPossibleException() {
		CinemaRoomEntity room = roomEntity();

		when(cinemaRoomRepository.findById(ROOM_ID)).thenReturn(Optional.of(room));
		when(screeningRepository.existsByCinemaRoomId(ROOM_ID)).thenReturn(true);

		assertThatThrownBy(() -> cinemaRoomService.updateCinemaRoom(roomRequest(), ROOM_ID))
				.isInstanceOf(CinemaRoomChangesNotPossibleException.class)
				.hasMessage("Changes are not possible for cinema room with id 1 screening exits");

		verify(cinemaRoomRepository).findById(ROOM_ID);
		verify(screeningRepository).existsByCinemaRoomId(ROOM_ID);
		verify(cinemaRoomRepository, never()).save(any());
		verify(seatRepository, never()).saveAll(anyList());
		verify(seatRepository, never()).deleteAllInBatch(anyList());
		verify(seatRepository, never()).flush();
	}

	@Test
	void deleteCinemaRoom_whenRoomExistsAndNoScreeningExists_deletesCinemaRoom() {
		CinemaRoomEntity room = roomEntity();

		when(cinemaRoomRepository.findById(ROOM_ID)).thenReturn(Optional.of(room));
		when(screeningRepository.existsByCinemaRoomId(ROOM_ID)).thenReturn(false);

		cinemaRoomService.deleteCinemaRoom(ROOM_ID);

		verify(cinemaRoomRepository).findById(ROOM_ID);
		verify(screeningRepository).existsByCinemaRoomId(ROOM_ID);
		verify(cinemaRoomRepository).delete(room);
		verify(cinemaRoomRepository, never()).save(any());
		verify(seatRepository, never()).saveAll(anyList());
	}

	@Test
	void deleteCinemaRoom_whenRoomExistsAndScreeningExists_throwsCinemaRoomChangesNotPossibleException() {
		CinemaRoomEntity room = roomEntity();

		when(cinemaRoomRepository.findById(ROOM_ID)).thenReturn(Optional.of(room));
		when(screeningRepository.existsByCinemaRoomId(ROOM_ID)).thenReturn(true);

		assertThatThrownBy(() -> cinemaRoomService.deleteCinemaRoom(ROOM_ID))
				.isInstanceOf(CinemaRoomChangesNotPossibleException.class)
				.hasMessage("Changes are not possible for cinema room with id 1 screening exits");

		verify(cinemaRoomRepository).findById(ROOM_ID);
		verify(screeningRepository).existsByCinemaRoomId(ROOM_ID);
		verify(cinemaRoomRepository, never()).delete(any(CinemaRoomEntity.class));
		verify(cinemaRoomRepository, never()).save(any());
		verify(seatRepository, never()).saveAll(anyList());
	}

	@Test
	void deleteCinemaRoom_whenRoomDoesNotExist_throwsCinemaRoomNotFoundException() {
		when(cinemaRoomRepository.findById(ROOM_ID)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> cinemaRoomService.deleteCinemaRoom(ROOM_ID))
				.isInstanceOf(CinemaRoomNotFoundException.class)
				.hasMessage("Cinema Room with ID 1 not found");

		verify(cinemaRoomRepository).findById(ROOM_ID);
		verify(cinemaRoomRepository, never()).delete(any());
	}

	@Test
	void getRoomEntity_whenRoomExists_returnsCinemaRoomEntity() {
		CinemaRoomEntity room = roomEntity();

		when(cinemaRoomRepository.findById(ROOM_ID)).thenReturn(Optional.of(room));

		CinemaRoomEntity result = cinemaRoomService.getRoomEntity(ROOM_ID);

		assertSame(room, result);

		verify(cinemaRoomRepository).findById(ROOM_ID);
	}

	@Test
	void getRoomEntity_whenRoomDoesNotExist_throwsCinemaRoomNotFoundException() {
		when(cinemaRoomRepository.findById(ROOM_ID)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> cinemaRoomService.getRoomEntity(ROOM_ID))
				.isInstanceOf(CinemaRoomNotFoundException.class)
				.hasMessage("Cinema Room with ID 1 not found");

		verify(cinemaRoomRepository).findById(ROOM_ID);
	}

	@Test
	void getRoomResponseById_whenRoomExists_returnsCinemaRoomResponse() {
		CinemaRoomEntity room = roomEntity();

		when(cinemaRoomRepository.findById(ROOM_ID)).thenReturn(Optional.of(room));

		CinemaRoomResponse response = cinemaRoomService.getRoomResponseById(ROOM_ID);

		assertNotNull(response);
		assertEquals(ROOM_ID, response.getId());
		assertEquals(ROOM_NUMBER, response.getRoomNumber());
		assertEquals(ROOM_CAPACITY, response.getRoomCapacity());

		verify(cinemaRoomRepository).findById(ROOM_ID);
	}

	private CinemaRoomRequest roomRequest() {
		return new CinemaRoomRequest(ROOM_NUMBER, ROWS, ROW_CAPACITY);
	}

	private CinemaRoomRequest updatedRoomRequest() {
		return new CinemaRoomRequest(ROOM_NUMBER, UPDATED_ROWS, UPDATED_ROW_CAPACITY);
	}

	private CinemaRoomEntity roomEntity() {
		return roomEntity(ROOM_NUMBER, ROWS, ROW_CAPACITY, new ArrayList<>());
	}

	private CinemaRoomEntity roomEntityWithOneExistingSeat() {
		List<SeatEntity> seats = new ArrayList<>();
		seats.add(seat(1, 1));
		return roomEntity(ROOM_NUMBER, ROWS, ROW_CAPACITY, seats);
	}

	private CinemaRoomEntity roomEntity(int roomNumber, int rows, int rowCapacity, List<SeatEntity> seats) {
		CinemaRoomEntity room = new CinemaRoomEntity(roomNumber, rows, rowCapacity, seats);
		room.setId(ROOM_ID);
		return room;
	}

	private SeatEntity seat(int rowNumber, int seatNumber) {
		SeatEntity seat = new SeatEntity();
		seat.setRowNumber(rowNumber);
		seat.setSeatNumber(seatNumber);
		return seat;
	}

	private void mockRoomSaveAssignsId() {
		when(cinemaRoomRepository.save(any(CinemaRoomEntity.class)))
				.thenAnswer(invocation -> {
					CinemaRoomEntity room = invocation.getArgument(0);
					room.setId(ROOM_ID);
					return room;
				});
	}

	private void mockRoomSaveReturnsSameEntity() {
		when(cinemaRoomRepository.save(any(CinemaRoomEntity.class)))
				.thenAnswer(invocation -> invocation.getArgument(0));
	}

	private void mockSaveAllSeats() {
		when(seatRepository.saveAll(anyList()))
				.thenAnswer(invocation -> invocation.getArgument(0));
	}
}
