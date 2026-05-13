package com.dare.cinema_booking_system.screenings.entity;

import com.dare.cinema_booking_system.rooms.entity.SeatEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor
@Entity
@Table(name = "screening_seats")
public class ScreeningSeatEntity {

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	private ScreeningSeatStatus screeningSeatStatus;

	@ManyToOne
	@JoinColumn(name = "seats_id")
	private SeatEntity seats;

	@ManyToOne
	@JoinColumn(name = "screening_id")
	private ScreeningsEntity screeningsEntity;

	public ScreeningSeatEntity(ScreeningsEntity screeningsEntity,
							   SeatEntity seats) {
		this.screeningsEntity = screeningsEntity;
		this.screeningSeatStatus = ScreeningSeatStatus.FREE;
		this.seats = seats;
	}
}
