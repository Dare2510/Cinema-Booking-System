package com.dare.cinema_booking_system.rooms.service;

import com.dare.cinema_booking_system.rooms.dto.CinemaRoomRequest;
import com.dare.cinema_booking_system.rooms.dto.CinemaRoomResponse;
import com.dare.cinema_booking_system.rooms.entity.CinemaRoomEntity;
import com.dare.cinema_booking_system.rooms.entity.SeatEntity;
import com.dare.cinema_booking_system.rooms.exceptions.CinemaRoomNotFoundException;
import com.dare.cinema_booking_system.rooms.repository.CinemaRoomRepository;
import com.dare.cinema_booking_system.rooms.repository.SeatRepository;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
public class CinemaRoomService {

	private final CinemaRoomRepository cinemaRoomRepository;
	private final SeatRepository seatRepository;
	private final ModelMapper modelMapper;

	public CinemaRoomResponse createCinemaRoom(CinemaRoomRequest cinemaRoomRequest) {
		List<SeatEntity> seatEntityList = new ArrayList<>();

		CinemaRoomEntity cinemaRoomEntity = new CinemaRoomEntity(
				cinemaRoomRequest.getRoomNumber(),
				cinemaRoomRequest.getRows(),
				cinemaRoomRequest.getRowCapacity(),
				seatEntityList
		);

		cinemaRoomEntity.setSeats(seatsGenerator(cinemaRoomEntity));
		cinemaRoomRepository.save(cinemaRoomEntity);

		return modelMapper.map(cinemaRoomEntity, CinemaRoomResponse.class);

	}

	private CinemaRoomEntity getRoom(Long roomId) {
		return cinemaRoomRepository.findById(roomId).orElseThrow(()
				-> new CinemaRoomNotFoundException(roomId));
	}

	private List<SeatEntity> seatsGenerator(CinemaRoomEntity cinemaRoomEntity) {
		List<SeatEntity> seatEntityList = new ArrayList<>();
		for(int i = 0; i<=cinemaRoomEntity.getRows(); i++) {
			for(int j = 0; j<=cinemaRoomEntity.getRowCapacity(); j++) {
				SeatEntity newSeatEntity = new SeatEntity();

				newSeatEntity.setCinemaRoom(cinemaRoomEntity);
				newSeatEntity.setRowNumber(i);
				newSeatEntity.setSeatNumber(j);
				seatEntityList.add(newSeatEntity);
				seatRepository.save(newSeatEntity);
			}
		}
		return seatEntityList;
	}
}
