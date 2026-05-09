package com.dare.cinema_booking_system.rooms.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter @Setter
@NoArgsConstructor
@Entity
@Table(name="cinemaRoom")
public class CinemaRoomEntity {

	@Id
	@GeneratedValue(strategy= GenerationType.IDENTITY)
	private Long id;

	@Column(name = "roomNumber",  nullable = false)
	private int roomNumber;

	@Column(name = "rows", nullable = false)
	private int rows;

	@Column(name = "rowCapacity", nullable = false)
	private int rowCapacity;

	@Column(name = "capacity",  nullable = false)
	private int capacity;

	@OneToMany(mappedBy = "cinemaRoom", cascade = CascadeType.ALL)
	private List<SeatEntity> seats;

	public CinemaRoomEntity(int roomNumber,int rows,int rowCapacity,List<SeatEntity> seats) {
		this.roomNumber = roomNumber;
		this.rows = rows;
		this.rowCapacity = rowCapacity;
		this.capacity = rows*rowCapacity;
		this.seats = seats;
	}
}
