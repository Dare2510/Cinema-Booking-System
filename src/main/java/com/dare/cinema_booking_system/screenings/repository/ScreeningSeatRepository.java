package com.dare.cinema_booking_system.screenings.repository;

import com.dare.cinema_booking_system.screenings.entity.ScreeningEntity;
import com.dare.cinema_booking_system.screenings.entity.ScreeningSeatEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ScreeningSeatRepository extends JpaRepository<ScreeningSeatEntity, Long> {

	@Query("SELECT COUNT(s) > 0 FROM ScreeningSeatEntity s WHERE s.screening.id = :screeningId AND s.screeningSeatStatus IN " +
			"(com.dare.cinema_booking_system.screenings.entity.ScreeningSeatStatus.RESERVED, com.dare.cinema_booking_system.screenings.entity.ScreeningSeatStatus.SOLD)")
	boolean hasReservedOrSoldSeats(@Param("screeningId") Long screeningId);

	List<ScreeningSeatEntity> getScreeningSeatsByScreening(ScreeningEntity screening);

	@Query("SELECT s FROM ScreeningSeatEntity s WHERE s.screening.id = :screeningId " +
			"AND s.screeningSeatStatus = com.dare.cinema_booking_system.screenings.entity.ScreeningSeatStatus.FREE")
	List<ScreeningSeatEntity> getFreeScreeningSeats(@Param("screeningId") Long screeningId);

	@Query("""
			    SELECT COUNT(s) = :seatCount
			    FROM ScreeningSeatEntity s
			    WHERE s.screening.id = :screeningId
			    AND s.cinemaSeats.id IN :seatIds
			    AND s.screeningSeatStatus = com.dare.cinema_booking_system.screenings.entity.ScreeningSeatStatus.FREE
			""")
	boolean areAllCinemaRoomSeatsFree(
			@Param("screeningId") Long screeningId,
			@Param("seatIds") List<Long> seatIds,
			@Param("seatCount") long seatCount
	);

}
