package com.dare.cinema_booking_system.screenings.entity;

import com.dare.cinema_booking_system.movies.entity.MovieEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
import java.util.List;

@Getter @Setter
@NoArgsConstructor
@Entity
@Table(name="screening")
public class ScreeningsEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "startTime", nullable = false)
	private Date startDate;

	@Column(name = "endTime", nullable = false)
	private Date endDate;

	@Enumerated(EnumType.STRING)
	@Column(name= "time_slot",nullable = false)
	private TimeSlot timeSlot;

	@OneToMany(mappedBy = "screeningsEntity", cascade = CascadeType.ALL)
	private List<ScreeningSeatEntity> screeningSeatEntities;

	@ManyToOne
	@JoinColumn(name="movies_id")
	private MovieEntity movie;

	public ScreeningsEntity(MovieEntity movie,Date startDate, Date endDate,TimeSlot timeSlot,
							List<ScreeningSeatEntity> screeningSeatEntities) {
		this.startDate = startDate;
		this.endDate = endDate;
		this.timeSlot = timeSlot;
		this.screeningSeatEntities = screeningSeatEntities;
		this.movie = movie;
	}
}
