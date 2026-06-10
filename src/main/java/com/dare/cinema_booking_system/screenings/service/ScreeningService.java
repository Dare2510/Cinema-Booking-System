package com.dare.cinema_booking_system.screenings.service;

import com.dare.cinema_booking_system.movie.dto.MovieResponse;
import com.dare.cinema_booking_system.movie.entity.MovieEntity;
import com.dare.cinema_booking_system.movie.service.MovieService;
import com.dare.cinema_booking_system.rooms.dto.CinemaRoomResponse;
import com.dare.cinema_booking_system.rooms.entity.CinemaRoomEntity;
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
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
public class ScreeningService {

	private final ScreeningRepository screeningRepository;
	private final ScreeningSeatService screeningSeatService;
	private final MovieService movieService;
	private final CinemaRoomService cinemaRoomService;

	public ScreeningResponse createScreening(ScreeningRequest screeningRequest) {

		MovieEntity movie = movieService.getMovieEntityById(screeningRequest.getMovieId());
		CinemaRoomEntity room = cinemaRoomService.getRoomEntity(screeningRequest.getRoomId());
		LocalDate date = screeningRequest.getScreeningDate();
		TimeSlot timeSlot = screeningRequest.getTimeSlot();
		BigDecimal price = screeningRequest.getPrice();

		boolean spotReserved = validateScreeningSpot(screeningRequest);

		if (!spotReserved) {
			ScreeningEntity screening = new ScreeningEntity(room.getId(), movie, date, timeSlot, price);
			screening.setTimes(timeSlot);
			screeningRepository.save(screening);
			screeningSeatService.createScreeningSeats(room, screening);

			log.info("Screening with {} id created successfully", screening.getId());
			return responseBuilder(screening);

		} else {
			log.warn("Screening spot on {} at {} in room with id {} is already reserved ", date, timeSlot, room.getId());
			throw new ScreeningSlotAlreadyBookedException(room.getId(), date, timeSlot);
		}
	}

	public ScreeningResponse updateScreening(Long screeningId, ScreeningRequest screeningRequest) {

		ScreeningEntity toUpdate = getScreeningEntity(screeningId);
		boolean hasReservation = screeningSeatService.validateScreeningUpdate(screeningId);
		boolean sameSpot = isSameSpot(toUpdate, screeningRequest);

		if (sameSpot && !hasReservation) {

			return setterForUpdate(toUpdate, screeningRequest);
		} else {

			if (!hasReservation) {
				boolean spotIsBooked = validateScreeningSpot(screeningRequest);

				if (!spotIsBooked) {
					return setterForUpdate(toUpdate, screeningRequest);

				} else {
					LocalDate requestedDate = screeningRequest.getScreeningDate();
					CinemaRoomEntity requestedRoom = cinemaRoomService.getRoomEntity(screeningRequest.getRoomId());
					TimeSlot requestedTime = screeningRequest.getTimeSlot();

					log.warn("Screening spot on {} at {} in room with ID {} is already reserved",
							requestedDate, requestedTime, requestedRoom);
					throw new ScreeningSlotAlreadyBookedException(requestedRoom.getId(), requestedDate, requestedTime);
				}
			} else {
				throw new ScreeningUpdateNotPossibleException(screeningId);
			}
		}


	}

	public void deleteScreeningById(Long screeningId) {

		boolean reservations = screeningSeatService.validateScreeningUpdate(screeningId);

		if (!reservations) {
			ScreeningEntity screening = getScreeningEntity(screeningId);
			screeningRepository.delete(screening);
			log.info("Deleted screening with {} ID", screeningId);

		} else {
			throw new ScreeningUpdateNotPossibleException(screeningId);
		}
	}

	public List<ScreeningResponse> getUpcomingScreenings() {
		LocalDate periodOfOneMonth = LocalDate.now().plusMonths(1);
		return screeningRepository.getUpcomingScreenings(periodOfOneMonth)
				.stream()
				.map(screenings ->
				{
					log.info("Getting upcoming Screenings");
					return responseBuilder(screenings);
				}).toList();
	}

	public Page<ScreeningResponse> getPageOfScreenings(Pageable pageable) {
		return screeningRepository.findAll(pageable)
				.map(screenings ->
				{
					log.info("Getting Page of Screenings");
					return responseBuilder(screenings);
				});
	}

	public ScreeningResponse getScreeningById(Long screeningId) {
		ScreeningEntity screening = getScreeningEntity(screeningId);
		log.info("Getting screening by {} ID", screeningId);
		return responseBuilder(screening);

	}

	//Helper Methods
	public ScreeningEntity getScreeningEntity(Long screeningId) {
		return screeningRepository.findById(screeningId).orElseThrow(
				() -> {
					log.info("Screening with ID {} not found", screeningId);
					return new ScreeningNotFoundException(screeningId);
				});
	}

	private ScreeningResponse setterForUpdate(ScreeningEntity toUpdate, ScreeningRequest screeningRequest) {

		MovieEntity requestedMovie = movieService.getMovieEntityById(screeningRequest.getMovieId());
		LocalDate requestedDate = screeningRequest.getScreeningDate();
		CinemaRoomEntity requestedRoom = cinemaRoomService.getRoomEntity(screeningRequest.getRoomId());
		TimeSlot requestedTime = screeningRequest.getTimeSlot();
		BigDecimal price = screeningRequest.getPrice();
		List<ScreeningSeatEntity> newUpdatedScreeningSeats = screeningSeatService.createScreeningSeats(requestedRoom, toUpdate);

		toUpdate.setMovie(requestedMovie);
		toUpdate.setScreeningDate(requestedDate);
		toUpdate.setTimeSlot(requestedTime);
		toUpdate.setCinemaRoomId(requestedRoom.getId());
		toUpdate.setScreeningSeats(newUpdatedScreeningSeats);
		toUpdate.setPrice(price);
		toUpdate.setTimes(requestedTime);

		screeningRepository.save(toUpdate);
		log.info("Screening with ID {} updated successfully", toUpdate.getId());
		return responseBuilder(toUpdate);
	}

	private ScreeningResponse responseBuilder(ScreeningEntity screeningEntity) {
		MovieEntity screeningMovie = screeningEntity.getMovie();

		Long roomId = screeningEntity.getCinemaRoomId();
		CinemaRoomEntity screeningRoom = cinemaRoomService.getRoomEntity(roomId);

		return ScreeningResponse.builder()
				.id(screeningEntity.getId())
				.screeningDate(screeningEntity.getScreeningDate())
				.price(screeningEntity.getPrice())
				.timeSlot(screeningEntity.getTimeSlot())
				.startTime(screeningEntity.getStartTime())
				.movieInformation(MovieResponse.builder()
						.title(screeningMovie.getTitle())
						.description(screeningMovie.getDescription())
						.duration(screeningMovie.getDuration())
						.genre(screeningMovie.getGenre())
						.build())
				.cinemaRoomInformation(CinemaRoomResponse.builder()
						.roomNumber(screeningRoom.getRoomNumber())
						.roomCapacity(screeningRoom.getRoomCapacity())
						.build()
				)
				.build();
	}
	//Validators

	private boolean validateScreeningSpot(ScreeningRequest screeningRequest) {
		return screeningRepository.existsByCinemaRoomIdAndScreeningDateAndTimeSlot
				(screeningRequest.getRoomId(), screeningRequest.getScreeningDate(), screeningRequest.getTimeSlot());

	}

	private boolean isSameSpot(ScreeningEntity screeningEntity, ScreeningRequest screeningRequest) {
		return screeningEntity.getScreeningDate().equals(screeningRequest.getScreeningDate())
				&& screeningEntity.getTimeSlot().equals(screeningRequest.getTimeSlot())
				&& screeningEntity.getCinemaRoomId().equals(screeningRequest.getRoomId());

	}

}
