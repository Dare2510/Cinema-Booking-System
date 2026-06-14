package com.dare.cinema_booking_system.reservations.repository;

import com.dare.cinema_booking_system.reservations.entity.ReservationEntity;
import com.dare.cinema_booking_system.reservations.entity.ReservationStatus;
import com.dare.cinema_booking_system.user.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface ReservationsRepository extends JpaRepository<ReservationEntity, Long> {

	boolean existsByUserAndReservationStatusIn(
			UserEntity user,
			List<ReservationStatus> statuses
	);
}
