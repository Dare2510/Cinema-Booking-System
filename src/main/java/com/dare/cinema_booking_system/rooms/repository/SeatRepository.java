package com.dare.cinema_booking_system.rooms.repository;

import com.dare.cinema_booking_system.rooms.entity.CinemaRoomEntity;
import com.dare.cinema_booking_system.rooms.entity.SeatEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SeatRepository extends JpaRepository<SeatEntity, Long> {

	List<SeatEntity> findByCinemaRoom(CinemaRoomEntity cinemaRoom);
}
