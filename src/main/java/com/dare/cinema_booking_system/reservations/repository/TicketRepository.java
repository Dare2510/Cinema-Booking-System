package com.dare.cinema_booking_system.reservations.repository;

import com.dare.cinema_booking_system.reservations.entity.TicketEntity;
import com.dare.cinema_booking_system.screenings.entity.ScreeningSeatEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TicketRepository extends JpaRepository<TicketEntity,Long> {

	Optional<TicketEntity> findByTicketNumber(String ticketNumber);

	@Query("SELECT t FROM TicketEntity t " +
			"WHERE t.ticketStatus = com.dare.cinema_booking_system.reservations.entity.TicketStatus.VALID " +
			"AND (t.reservation.screening.screeningDate < :currentDate " +
			"OR (t.reservation.screening.screeningDate = :currentDate AND t.reservation.screening.endTime < :currentTime))")
	List<TicketEntity> getExpiredTickets(
			@Param("currentDate") LocalDate currentDate,
			@Param("currentTime") LocalTime currentTime
	);


}
