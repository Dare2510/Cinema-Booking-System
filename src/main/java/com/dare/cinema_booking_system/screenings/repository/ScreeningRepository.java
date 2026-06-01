package com.dare.cinema_booking_system.screenings.repository;

import com.dare.cinema_booking_system.screenings.entity.ScreeningEntity;
import com.dare.cinema_booking_system.screenings.entity.TimeSlot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;

public interface ScreeningRepository extends JpaRepository<ScreeningEntity, Long> {

	boolean existsByCinemaRoomIdAndScreeningDateAndTimeSlot(Long cinemaRoomId, LocalDate date, TimeSlot timeSlot);

	boolean existsByCinemaRoomId(Long cinemaRoomId);


}
