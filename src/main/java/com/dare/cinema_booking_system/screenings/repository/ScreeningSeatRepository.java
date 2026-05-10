package com.dare.cinema_booking_system.screenings.repository;

import com.dare.cinema_booking_system.screenings.entity.ScreeningSeatEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ScreeningSeatRepository extends JpaRepository<ScreeningSeatEntity,Long> {
}
