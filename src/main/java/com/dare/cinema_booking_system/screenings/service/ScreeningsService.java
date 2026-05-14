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
import com.dare.cinema_booking_system.screenings.repository.ScreeningSeatRepository;
import com.dare.cinema_booking_system.screenings.repository.ScreeningsRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

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

	public ScreeningsResponse createScreenings(ScreeningsRequest screeningsRequest) {
		MovieEntity movieForScreening = movieService.getMovieEntityById(screeningsRequest.getMovieId());
		CinemaRoomEntity roomForScreening = cinemaRoomService.getRoomEntity(screeningsRequest.getRoomId());
		LocalDate dateForScreening = screeningsRequest.getScreeningDate();
		TimeSlot  timeSlotForScreening = screeningsRequest.getTimeSlot();

		boolean validateScreeningSpot =validateScreeningSpot(screeningsRequest);

		if(!validateScreeningSpot) {
		ScreeningsEntity newScreeningEntity = new ScreeningsEntity(roomForScreening.getId(), movieForScreening,dateForScreening,timeSlotForScreening);
		screeningsRepository.save(newScreeningEntity);
		createScreeningSeats(roomForScreening, newScreeningEntity);

		log.info("Screening created successfully");
		return modelMapper.map(newScreeningEntity, ScreeningsResponse.class);

		} else{
			log.warn("Screening spot not available");
			throw new ScreeningSlotAlreadyBookedException(roomForScreening.getId(),dateForScreening, timeSlotForScreening);
		}
	}


	//Helper
	private ScreeningsEntity getScreeningEntity(Long screeningId) {
		return screeningsRepository.findById(screeningId).orElseThrow(
				() -> {
					log.info("Screening with {} not found", screeningId);
					return new ScreeningNotFoundException(screeningId);
				});
	}

	private List<ScreeningSeatEntity> createScreeningSeats(CinemaRoomEntity cinemaRoom, ScreeningsEntity screeningsEntity) {
		List<SeatEntity> seats = cinemaRoom.getSeats();
		List<ScreeningSeatEntity> screeningSeats = screeningsEntity.getScreeningSeatEntities();
		for(SeatEntity seat : seats) {
			screeningSeats.add(new ScreeningSeatEntity(screeningsEntity, seat));
		}
		log.info("Seats for screening with {} id created successfully", screeningsEntity.getId());
		screeningSeatRepository.saveAll(screeningSeats);
		return screeningSeats;
	}

	private boolean validateScreeningSpot(ScreeningsRequest screeningsRequest) {
		return screeningsRepository.existsByCinemaRoomIdAndScreeningDateAndTimeSlot
				(screeningsRequest.getRoomId(),screeningsRequest.getScreeningDate(),screeningsRequest.getTimeSlot());

	}
}
