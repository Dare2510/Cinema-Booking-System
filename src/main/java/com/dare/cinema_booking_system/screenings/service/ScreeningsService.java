package com.dare.cinema_booking_system.screenings.service;

import com.dare.cinema_booking_system.movies.entity.MovieEntity;
import com.dare.cinema_booking_system.movies.service.MovieService;
import com.dare.cinema_booking_system.rooms.entity.CinemaRoomEntity;
import com.dare.cinema_booking_system.rooms.entity.SeatEntity;
import com.dare.cinema_booking_system.rooms.service.CinemaRoomService;
import com.dare.cinema_booking_system.screenings.dto.ScreeningsRequest;
import com.dare.cinema_booking_system.screenings.dto.ScreeningsResponse;
import com.dare.cinema_booking_system.screenings.entity.ScreeningSeatEntity;
import com.dare.cinema_booking_system.screenings.entity.ScreeningsEntity;
import com.dare.cinema_booking_system.screenings.entity.TimeSlot;
import com.dare.cinema_booking_system.screenings.exceptions.ScreeningNotFoundException;
import com.dare.cinema_booking_system.screenings.exceptions.ScreeningSlotAlreadyBookedException;
import com.dare.cinema_booking_system.screenings.exceptions.ScreeningUpdateNotPossibleException;
import com.dare.cinema_booking_system.screenings.repository.ScreeningSeatRepository;
import com.dare.cinema_booking_system.screenings.repository.ScreeningsRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
public class ScreeningsService {

	private final ScreeningsRepository screeningsRepository;
	private final ScreeningSeatRepository screeningSeatRepository;
	private final MovieService movieService;
	private final CinemaRoomService cinemaRoomService;
	private final ModelMapper modelMapper;

	public Page<ScreeningsResponse> getPageOfScreenings(Pageable pageable) {
		return screeningsRepository.findAll(pageable)
				.map(screenings ->
				{
					log.info("Getting Page of Screenings");
					return modelMapper.map(screenings, ScreeningsResponse.class);
				});
	}

	public ScreeningsResponse getScreeningById(Long screeningId) {
		ScreeningsEntity screening = getScreeningEntity(screeningId);
		log.info("Getting screening by {} ID", screeningId);
		return modelMapper.map(screening, ScreeningsResponse.class);

	}

	public void deleteScreeningById(Long screeningId) {

		boolean reservations = validateScreeningUpdate(screeningId);

		if (!reservations) {
			ScreeningsEntity screening = getScreeningEntity(screeningId);
			screeningsRepository.delete(screening);
			log.info("Deleted screening with {} ID", screeningId);

		} else {
			throw new ScreeningUpdateNotPossibleException(screeningId);
		}
	}

	public ScreeningsResponse createScreenings(ScreeningsRequest screeningsRequest) {

		MovieEntity movie = movieService.getMovieEntityById(screeningsRequest.getMovieId());
		CinemaRoomEntity room = cinemaRoomService.getRoomEntity(screeningsRequest.getRoomId());
		LocalDate date = screeningsRequest.getScreeningDate();
		TimeSlot timeSlot = screeningsRequest.getTimeSlot();
		BigDecimal price = screeningsRequest.getPrice();

		boolean spotReserved = validateScreeningSpot(screeningsRequest);

		if (!spotReserved) {
			ScreeningsEntity screening = new ScreeningsEntity(room.getId(), movie, date, timeSlot, price);
			screeningsRepository.save(screening);
			createScreeningSeats(room, screening);

			log.info("Screening with {} id created successfully", screening.getId());
			return modelMapper.map(screening, ScreeningsResponse.class);

		} else {
			log.warn("Screening spot on {} at {} in room with id {} is already reserved ", date, timeSlot, room.getId());
			throw new ScreeningSlotAlreadyBookedException(room.getId(), date, timeSlot);
		}
	}

	public ScreeningsResponse updateScreenings(Long screeningId, ScreeningsRequest screeningsRequest) {

		ScreeningsEntity toUpdate = getScreeningEntity(screeningId);
		MovieEntity requestedMovie = movieService.getMovieEntityById(screeningsRequest.getMovieId());
		LocalDate requestedDate = screeningsRequest.getScreeningDate();
		CinemaRoomEntity requestedRoom = cinemaRoomService.getRoomEntity(screeningsRequest.getRoomId());
		TimeSlot requestedTime = screeningsRequest.getTimeSlot();
		BigDecimal price = screeningsRequest.getPrice();

		boolean hasReservation = validateScreeningUpdate(screeningId);

		if (!hasReservation) {
			boolean spotIsBooked = validateScreeningSpot(screeningsRequest);

			if (!spotIsBooked) {
				List<ScreeningSeatEntity> newUpdatedScreeningSeats = createScreeningSeats(requestedRoom, toUpdate);

				toUpdate.setMovie(requestedMovie);
				toUpdate.setScreeningDate(requestedDate);
				toUpdate.setTimeSlot(requestedTime);
				toUpdate.setCinemaRoomId(requestedRoom.getId());
				toUpdate.setScreeningSeats(newUpdatedScreeningSeats);
				toUpdate.setPrice(price);

				screeningsRepository.save(toUpdate);
				log.info("Screening with ID {} updated successfully", screeningId);
				return modelMapper.map(toUpdate, ScreeningsResponse.class);
			} else {
				log.warn("Screening spot on {} at {} in room with ID {} is already reserved", requestedDate, requestedTime, requestedRoom.getId());
				throw new ScreeningSlotAlreadyBookedException(requestedRoom.getId(), requestedDate, requestedTime);
			}
		} else {
			throw new ScreeningUpdateNotPossibleException(screeningId);
		}


	}

	//Helper
	private ScreeningsEntity getScreeningEntity(Long screeningId) {
		return screeningsRepository.findById(screeningId).orElseThrow(
				() -> {
					log.info("Screening with ID {} not found", screeningId);
					return new ScreeningNotFoundException(screeningId);
				});
	}

	private List<ScreeningSeatEntity> createScreeningSeats(CinemaRoomEntity cinemaRoom, ScreeningsEntity screening) {
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

	private boolean validateScreeningSpot(ScreeningsRequest screeningsRequest) {
		return screeningsRepository.existsByCinemaRoomIdAndScreeningDateAndTimeSlot
				(screeningsRequest.getRoomId(), screeningsRequest.getScreeningDate(), screeningsRequest.getTimeSlot());

	}

	private boolean validateScreeningUpdate(Long screeningId) {
		return screeningSeatRepository.hasReservedOrSoldSeats(screeningId);
	}

}
