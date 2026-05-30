package com.dare.cinema_booking_system.reservations.repository;

import com.dare.cinema_booking_system.reservations.entity.TicketEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TicketRepository extends JpaRepository<TicketEntity,Long> {

	Optional<TicketEntity> findByReservation_Id(Long reservationId);
	Optional<TicketEntity> findByTicketNumber(String ticketNumber);
}
