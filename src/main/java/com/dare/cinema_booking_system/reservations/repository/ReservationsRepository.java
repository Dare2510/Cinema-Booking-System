package com.dare.cinema_booking_system.reservations.repository;

import com.dare.cinema_booking_system.reservations.entity.PaymentStatus;
import com.dare.cinema_booking_system.reservations.entity.ReservationEntity;
import com.dare.cinema_booking_system.reservations.entity.ReservationStatus;
import com.dare.cinema_booking_system.user.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface ReservationsRepository extends JpaRepository<ReservationEntity, Long> {

	@Query("""
        SELECT COUNT(r) = 0
        FROM ReservationEntity r
        JOIN r.payment p
        WHERE r.user = :user
          AND NOT (
              r.reservationStatus = com.dare.cinema_booking_system.reservations.entity.ReservationStatus.CANCELLED
              AND p.paymentStatus = com.dare.cinema_booking_system.reservations.entity.PaymentStatus.REFUNDED
          )
        """)
	boolean userCanBeDeleted(@Param("user") UserEntity user);
}
