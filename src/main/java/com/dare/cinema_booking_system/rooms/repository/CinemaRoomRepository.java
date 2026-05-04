package com.dare.cinema_booking_system.rooms.repository;

import com.dare.cinema_booking_system.rooms.entity.CinemaRoomEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CinemaRoomRepository extends JpaRepository<CinemaRoomEntity, Long> {
}
