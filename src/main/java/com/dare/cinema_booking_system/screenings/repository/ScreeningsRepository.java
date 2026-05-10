package com.dare.cinema_booking_system.screenings.repository;

import com.dare.cinema_booking_system.screenings.entity.ScreeningsEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ScreeningsRepository extends JpaRepository<ScreeningsEntity,Long> {
}
