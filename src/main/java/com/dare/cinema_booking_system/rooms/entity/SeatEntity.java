package com.dare.cinema_booking_system.rooms.entity;

import com.dare.cinema_booking_system.screenings.entity.ScreeningSeatEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "seats")
public class SeatEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "rowNumber", nullable = false)
	private int rowNumber;

	@Column(name = "seatNumber", nullable = false)
	private int seatNumber;

	@ManyToOne
	@JoinColumn(name = "cinemaRoom_id")
	private CinemaRoomEntity cinemaRoom;

	@OneToMany(mappedBy = "cinemaSeats", cascade = CascadeType.ALL)
	private List<ScreeningSeatEntity> screeningSeats;

	public SeatEntity(int rowNumber, int seatNumber, CinemaRoomEntity cinemaRoom) {
		this.rowNumber = rowNumber;
		this.seatNumber = seatNumber;
		this.cinemaRoom = cinemaRoom;
		this.screeningSeats = new ArrayList<>();
	}
}
