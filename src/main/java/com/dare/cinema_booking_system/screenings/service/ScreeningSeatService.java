package com.dare.cinema_booking_system.screenings.service;

import com.dare.cinema_booking_system.reservations.dto.ReservationRequest;
import com.dare.cinema_booking_system.rooms.entity.CinemaRoomEntity;
import com.dare.cinema_booking_system.rooms.entity.SeatEntity;
import com.dare.cinema_booking_system.screenings.dto.ScreeningSeatResponse;
import com.dare.cinema_booking_system.screenings.entity.ScreeningEntity;
import com.dare.cinema_booking_system.screenings.entity.ScreeningSeatEntity;
import com.dare.cinema_booking_system.screenings.entity.ScreeningSeatStatus;
import com.dare.cinema_booking_system.screenings.exceptions.ScreeningSeatNotAvailableException;
import com.dare.cinema_booking_system.screenings.repository.ScreeningSeatRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
public class ScreeningSeatService {

	private ScreeningSeatRepository screeningSeatRepository;


	public List<ScreeningSeatEntity> createScreeningSeats(CinemaRoomEntity cinemaRoom, ScreeningEntity screening) {
		List<SeatEntity> cinemaSeats = cinemaRoom.getSeats();
		List<ScreeningSeatEntity> screeningSeats = screening.getScreeningSeats();
		if (!screeningSeats.isEmpty()) {
			List<ScreeningSeatEntity> oldScreeningSeats = screeningSeatRepository.getScreeningSeatsByScreening(screening);
			screeningSeatRepository.deleteAllInBatch(oldScreeningSeats);
			screeningSeatRepository.flush();
			screeningSeats.clear();
			log.info("Existing screening seats cleared");
		}

		for (SeatEntity cinemaSeat : cinemaSeats) {
			screeningSeats.add(new ScreeningSeatEntity(screening, cinemaSeat));
		}
		log.info("Seats for screening with {} ID created successfully", screening.getId());
		screeningSeatRepository.saveAll(screeningSeats);
		return screeningSeats;
	}

	public List<ScreeningSeatResponse> getFreeScreeningSeatsByScreeningId(Long screeningId) {
		List<ScreeningSeatEntity> freeSeats = screeningSeatRepository.getFreeScreeningSeats(screeningId);

		log.info("Get free screening seats with screening id {}", screeningId);
		return freeSeats.stream()
				.map(seat -> ScreeningSeatResponse.builder()
						.cinemaRoomSeatId(seat.getCinemaSeats().getId())
						.seatNumber(seat.getCinemaSeats().getSeatNumber())
						.rowNumber(seat.getCinemaSeats().getRowNumber())
						.build()
				)
				.toList();
	}

	public List<ScreeningSeatEntity> lockAndUpdateSeats(ScreeningEntity screeningToReserve, ReservationRequest reservationRequest) {
		List<ScreeningSeatEntity> lockedSeats = screeningSeatRepository.findFreeSeatsForReservationWithLock(screeningToReserve.getId(),reservationRequest.getCinemaRoomSeatIds());

		List<Long> targetSeatIds = reservationRequest.getCinemaRoomSeatIds();

		boolean allSeatsFree = lockedSeats.size() == targetSeatIds.size();

		if (allSeatsFree) {
			lockedSeats.forEach(seat -> { seat.setScreeningSeatStatus(ScreeningSeatStatus.RESERVED); });
			screeningSeatRepository.saveAll(lockedSeats);
			log.info("Seats with id's {} have been updated", targetSeatIds);
		} else {
			log.warn("Reservation failed , not all seats are available");
			throw new ScreeningSeatNotAvailableException();
		}

		return lockedSeats;

	}

	//Validators

	public boolean validateScreeningUpdate(Long screeningId) {
		return screeningSeatRepository.hasReservedOrSoldSeats(screeningId);
	}

	public boolean seatsAreFree(ScreeningEntity screeningToReserve, ReservationRequest reservationRequest) {
		return screeningSeatRepository.areAllCinemaRoomSeatsFree(screeningToReserve.getId(),
				reservationRequest.getCinemaRoomSeatIds(), reservationRequest.getCinemaRoomSeatIds().size());
	}
}
