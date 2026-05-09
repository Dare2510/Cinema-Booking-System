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
	private Date startTime;

	@Column(name = "endTime", nullable = false)
	private Date endTime;

	@OneToMany(mappedBy = "screening", cascade = CascadeType.ALL)
	private List<ScreeningSeatEntity> screeningSeatEntities;

	@ManyToOne
	@JoinColumn(name="movies_id")
	private MovieEntity movie;

	public ScreeningsEntity(Long id, Date startTime, Date endTime,
							List<ScreeningSeatEntity> screeningSeatEntities, MovieEntity movie) {
		this.id = id;
		this.startTime = startTime;
		this.endTime = endTime;
		this.screeningSeatEntities = screeningSeatEntities;
		this.movie = movie;
	}
}
