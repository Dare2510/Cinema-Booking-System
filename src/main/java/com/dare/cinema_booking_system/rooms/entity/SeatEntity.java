package com.dare.cinema_booking_system.rooms.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "Seats")
@Getter
@Setter
@NoArgsConstructor
public class SeatEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "rowNumber",nullable = false)
	private int rowNumber;

	@Column(name= "seatNumber",nullable = false)
	private int seatNumber;

	@ManyToOne
	@JoinColumn(name = "cinemaRoom_id")
	private CinemaRoomEntity cinemaRoom;

	public SeatEntity(int rowNumber, int seatNumber, CinemaRoomEntity cinemaRoom) {
		this.rowNumber = rowNumber;
		this.seatNumber = seatNumber;
		this.cinemaRoom = cinemaRoom;
	}
}
