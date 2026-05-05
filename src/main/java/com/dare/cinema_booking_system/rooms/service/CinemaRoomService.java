package com.dare.cinema_booking_system.rooms.service;

import com.dare.cinema_booking_system.rooms.dto.CinemaRoomRequest;
import com.dare.cinema_booking_system.rooms.dto.CinemaRoomResponse;
import com.dare.cinema_booking_system.rooms.entity.CinemaRoomEntity;
import com.dare.cinema_booking_system.rooms.entity.SeatEntity;
import com.dare.cinema_booking_system.rooms.exceptions.CinemaRoomNotFoundException;
import com.dare.cinema_booking_system.rooms.exceptions.CinemaRoomNumberDuplicateException;
import com.dare.cinema_booking_system.rooms.repository.CinemaRoomRepository;
import com.dare.cinema_booking_system.rooms.repository.SeatRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
public class CinemaRoomService {

	private final CinemaRoomRepository cinemaRoomRepository;
	private final SeatRepository seatRepository;
	private final ModelMapper modelMapper;

	@Transactional
	public CinemaRoomResponse createCinemaRoom(CinemaRoomRequest cinemaRoomRequest) {
		int roomNumber = cinemaRoomRequest.getRoomNumber();
		boolean roomExists = validateRoomNumber(roomNumber);

		if (!roomExists) {
			CinemaRoomEntity newRoom = entityCreator(cinemaRoomRequest);

			seatsGenerator(newRoom,newRoom.getSeats());

			return responseMapper(newRoom);

		} else {
			throw new CinemaRoomNumberDuplicateException(roomNumber);
		}


	}
	public Page<CinemaRoomResponse> getPageOfCinemaRooms(Pageable pageable) {
		return cinemaRoomRepository.findAll(pageable).
				map(entity -> modelMapper.map(entity, CinemaRoomResponse.class));
	}

	public CinemaRoomResponse updateCinemaRoom(CinemaRoomRequest cinemaRoomRequest, Long roomId) {
		CinemaRoomEntity roomToUpdate = getRoom(roomId);
		setterRoomValues(cinemaRoomRequest, roomToUpdate);
		seatsGenerator(roomToUpdate, roomToUpdate.getSeats());
		return responseMapper(roomToUpdate);

	}

	// Helper Methods
	private CinemaRoomEntity getRoom(Long roomId) {
		return cinemaRoomRepository.findById(roomId).orElseThrow(()
				-> new CinemaRoomNotFoundException(roomId));
	}

	private List<SeatEntity> seatsGenerator(CinemaRoomEntity cinemaRoomEntity, List<SeatEntity> seatEntityList) {

		for (int i = 1; i <= cinemaRoomEntity.getRows(); i++) {
			for (int j = 1; j <= cinemaRoomEntity.getRowCapacity(); j++) {
				SeatEntity newSeatEntity = new SeatEntity();

				newSeatEntity.setCinemaRoom(cinemaRoomEntity);
				newSeatEntity.setRowNumber(i);
				newSeatEntity.setSeatNumber(j);
				seatEntityList.add(newSeatEntity);
				seatRepository.save(newSeatEntity);
			}
		}
		cinemaRoomRepository.save(cinemaRoomEntity);
		return seatEntityList;
	}

	private boolean validateRoomNumber(int roomNumber) {
		return cinemaRoomRepository.existsByRoomNumber(roomNumber);
	}

	private CinemaRoomResponse responseMapper(CinemaRoomEntity room) {
		return modelMapper.map(room, CinemaRoomResponse.class);
	}

	private void setterRoomValues(CinemaRoomRequest cinemaRoomRequest, CinemaRoomEntity cinemaRoomEntity) {
		int updatedRoomNumber = cinemaRoomRequest.getRoomNumber();
		int updatedNumberOfRows = cinemaRoomRequest.getRows();
		int updatedRowCapacity = cinemaRoomRequest.getRowCapacity();

		cinemaRoomEntity.setRoomNumber(updatedRoomNumber);
		cinemaRoomEntity.setRows(updatedNumberOfRows);
		cinemaRoomEntity.setRowCapacity(updatedRowCapacity);
		cinemaRoomRepository.save(cinemaRoomEntity);
	}

	private CinemaRoomEntity entityCreator(CinemaRoomRequest cinemaRoomRequest) {
		List<SeatEntity> listOfSeats = new ArrayList<>();
		CinemaRoomEntity newRoom =  new CinemaRoomEntity(
				cinemaRoomRequest.getRoomNumber(),
				cinemaRoomRequest.getRows(),
				cinemaRoomRequest.getRowCapacity(),
				listOfSeats
		);

		cinemaRoomRepository.save(newRoom);
		return newRoom;
	}
}
