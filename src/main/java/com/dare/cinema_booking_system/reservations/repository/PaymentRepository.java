package com.dare.cinema_booking_system.reservations.repository;

import com.dare.cinema_booking_system.reservations.entity.PaymentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentRepository extends JpaRepository<PaymentEntity,Long> {
}
