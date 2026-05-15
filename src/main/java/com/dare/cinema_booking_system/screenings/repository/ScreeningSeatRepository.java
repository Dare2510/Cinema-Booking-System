package com.dare.cinema_booking_system.screenings.repository;

import com.dare.cinema_booking_system.screenings.entity.ScreeningSeatEntity;
import com.dare.cinema_booking_system.screenings.entity.ScreeningsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ScreeningSeatRepository extends JpaRepository<ScreeningSeatEntity, Long> {

	@Query("SELECT COUNT(s) > 0 FROM ScreeningSeatEntity s WHERE s.screening.id = :screeningId AND s.screeningSeatStatus IN " +
			"(com.dare.cinema_booking_system.screenings.entity.ScreeningSeatStatus.RESERVED, com.dare.cinema_booking_system.screenings.entity.ScreeningSeatStatus.SOLD)")
	boolean hasReservedOrSoldSeats(@Param("screeningId") Long screeningId);

	List<ScreeningSeatEntity> getScreeningSeatsByScreening(ScreeningsEntity screening);
}
