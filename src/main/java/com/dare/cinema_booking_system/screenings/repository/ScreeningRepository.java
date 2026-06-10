package com.dare.cinema_booking_system.screenings.repository;

import com.dare.cinema_booking_system.screenings.entity.ScreeningEntity;
import com.dare.cinema_booking_system.screenings.entity.TimeSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface ScreeningRepository extends JpaRepository<ScreeningEntity, Long> {

	boolean existsByCinemaRoomIdAndScreeningDateAndTimeSlot(Long cinemaRoomId, LocalDate date, TimeSlot timeSlot);

	boolean existsByCinemaRoomId(Long cinemaRoomId);

	boolean existsByMovieId(Long movieId);

	@Query("SELECT s FROM ScreeningEntity s " +
			"WHERE s.screeningDate BETWEEN CURRENT DATE AND :screeningDate")
	List<ScreeningEntity> getUpcomingScreenings(@Param("screeningDate") LocalDate screenDate);

}
