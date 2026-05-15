package com.dare.cinema_booking_system.screenings.entity;

import com.dare.cinema_booking_system.movies.entity.MovieEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "screening")
public class ScreeningsEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "cinema_room_id", nullable = false)
	private Long cinemaRoomId;

	@Column(name = "screening_date", nullable = false)
	private LocalDate screeningDate;

	@Column(name = "price", nullable = false)
	private BigDecimal price;

	@Enumerated(EnumType.STRING)
	@Column(name = "time_slot", nullable = false)
	private TimeSlot timeSlot;

	@OneToMany(mappedBy = "screening", cascade = CascadeType.ALL)
	private List<ScreeningSeatEntity> screeningSeats;

	@ManyToOne
	@JoinColumn(name = "movies_id")
	private MovieEntity movie;

	public ScreeningsEntity(Long cinemaRoomId, MovieEntity movie, LocalDate screeningDate, TimeSlot timeSlot, BigDecimal price) {
		this.cinemaRoomId = cinemaRoomId;
		this.screeningDate = screeningDate;
		this.timeSlot = timeSlot;
		this.price = price;
		this.screeningSeats = new ArrayList<>();
		this.movie = movie;
	}
}
