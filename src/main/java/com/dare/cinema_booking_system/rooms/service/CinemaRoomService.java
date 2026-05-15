package com.dare.cinema_booking_system.rooms.service;

import com.dare.cinema_booking_system.rooms.dto.CinemaRoomRequest;
import com.dare.cinema_booking_system.rooms.dto.CinemaRoomResponse;
import com.dare.cinema_booking_system.rooms.entity.CinemaRoomEntity;
import com.dare.cinema_booking_system.rooms.entity.SeatEntity;
import com.dare.cinema_booking_system.rooms.exceptions.CinemaRoomChangesNotPossibleException;
import com.dare.cinema_booking_system.rooms.exceptions.CinemaRoomNotFoundException;
import com.dare.cinema_booking_system.rooms.exceptions.CinemaRoomNumberDuplicateException;
import com.dare.cinema_booking_system.rooms.repository.CinemaRoomRepository;
import com.dare.cinema_booking_system.rooms.repository.SeatRepository;
import com.dare.cinema_booking_system.screenings.entity.ScreeningsEntity;
import com.dare.cinema_booking_system.screenings.repository.ScreeningsRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
public class CinemaRoomService {

	private final CinemaRoomRepository cinemaRoomRepository;
	private final SeatRepository seatRepository;
	private final ScreeningsRepository screeningRepository;
	private final ModelMapper modelMapper;

	@Transactional
	public CinemaRoomResponse createCinemaRoom(CinemaRoomRequest cinemaRoomRequest) {
		int roomNumber = cinemaRoomRequest.getRoomNumber();
		boolean roomExists = validateRoomNumber(roomNumber);

		if (!roomExists) {
			CinemaRoomEntity newRoom = entityCreator(cinemaRoomRequest);

			seatsGenerator(newRoom, newRoom.getSeats());

			return responseMapper(newRoom);

		} else {
			log.warn("Room number {} already exists", roomNumber);
			throw new CinemaRoomNumberDuplicateException(roomNumber);
		}


	}

	public Page<CinemaRoomResponse> getPageOfCinemaRooms(Pageable pageable) {
		return cinemaRoomRepository.findAll(pageable).
				map(entity -> {
					log.info("Getting Page of Cinema Rooms");
					return modelMapper.map(entity, CinemaRoomResponse.class);
				});
	}

	@Transactional
	public CinemaRoomResponse updateCinemaRoom(CinemaRoomRequest cinemaRoomRequest, Long roomId) {
		CinemaRoomEntity toUpdate = getRoomEntity(roomId);
		boolean screeningExists = validateScreeningExists(roomId);
		if (!screeningExists) {
			setterRoomValues(cinemaRoomRequest, toUpdate);
			seatsGenerator(toUpdate, toUpdate.getSeats());
			return responseMapper(toUpdate);
		} else {
			log.warn("Cinema Room with ID {} has Screening, updating not possible", roomId);
			throw new CinemaRoomChangesNotPossibleException(roomId);
		}
	}

	@Transactional
	public void deleteCinemaRoom(Long roomId) {
		CinemaRoomEntity toDelete = getRoomEntity(roomId);
		boolean screeningExists = validateScreeningExists(roomId);

		if (!screeningExists) {
			cinemaRoomRepository.delete(toDelete);
			log.info("Cinema Room with ID {} deleted", roomId);
		} else {
			log.warn("Cinema Room with ID {} has Screening, deleting not possible", roomId);
			throw new CinemaRoomChangesNotPossibleException(roomId);
		}
	}

	public CinemaRoomResponse getRoomResponseById(Long roomId) {
		CinemaRoomEntity room = getRoomEntity(roomId);
		return responseMapper(room);
	}


	// Helper Methods

	public CinemaRoomEntity getRoomEntity(Long roomId) {
		return cinemaRoomRepository.findById(roomId).orElseThrow(()
				-> {
			log.warn("Room ID {} does not exist", roomId);
			return new CinemaRoomNotFoundException(roomId);

		});
	}


	private void seatsGenerator(CinemaRoomEntity cinemaRoomEntity, List<SeatEntity> seatEntityList) {
		if (!seatEntityList.isEmpty()) {
			List<SeatEntity> oldSeats = seatRepository.findByCinemaRoom(cinemaRoomEntity);
			seatRepository.deleteAllInBatch(oldSeats);
			seatRepository.flush();
			seatEntityList.clear();
			log.info("Existing seats for room id {} cleared", cinemaRoomEntity.getId());
		}
		for (int i = 1; i <= cinemaRoomEntity.getRows(); i++) {
			for (int j = 1; j <= cinemaRoomEntity.getRowCapacity(); j++) {
				SeatEntity newSeats = new SeatEntity();

				newSeats.setCinemaRoom(cinemaRoomEntity);
				newSeats.setRowNumber(i);
				newSeats.setSeatNumber(j);
				seatEntityList.add(newSeats);

			}
		}
		seatRepository.saveAll(seatEntityList);
		log.info("Seats for room id {} saved successfully", cinemaRoomEntity.getId());
		cinemaRoomRepository.save(cinemaRoomEntity);

	}

	private boolean validateRoomNumber(int roomNumber) {
		return cinemaRoomRepository.existsByRoomNumber(roomNumber);
	}

	private CinemaRoomResponse responseMapper(CinemaRoomEntity room) {
		return modelMapper.map(room, CinemaRoomResponse.class);
	}

	private void setterRoomValues(CinemaRoomRequest cinemaRoomRequest, CinemaRoomEntity room) {
		int updatedRoomNumber = cinemaRoomRequest.getRoomNumber();
		int updatedNumberOfRows = cinemaRoomRequest.getRows();
		int updatedRowCapacity = cinemaRoomRequest.getRowCapacity();

		room.setRoomNumber(updatedRoomNumber);
		room.setRows(updatedNumberOfRows);
		room.setRowCapacity(updatedRowCapacity);
		room.setCapacity(updatedRowCapacity * updatedNumberOfRows);

		cinemaRoomRepository.save(room);
		log.info("Room number {} updated", updatedRoomNumber);
	}

	private CinemaRoomEntity entityCreator(CinemaRoomRequest cinemaRoomRequest) {
		List<SeatEntity> seats = new ArrayList<>();
		CinemaRoomEntity newRoom = new CinemaRoomEntity(
				cinemaRoomRequest.getRoomNumber(),
				cinemaRoomRequest.getRows(),
				cinemaRoomRequest.getRowCapacity(),
				seats
		);

		cinemaRoomRepository.save(newRoom);
		log.info("Cinema room with {} ID created", newRoom.getId());
		return newRoom;
	}

	private boolean validateScreeningExists(Long roomId) {

		return screeningRepository.existsByCinemaRoomId(roomId);
	}
}
