package com.dare.cinema_booking_system.movies.entity;

import com.dare.cinema_booking_system.screenings.entity.ScreeningSeatEntity;
import com.dare.cinema_booking_system.screenings.entity.ScreeningsEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter @Setter
@NoArgsConstructor
@Entity
@Table(name = "movies")
public class MovieEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name="title",nullable = false)
	private String title;

	@Column(name="description",nullable = false)
	private String description;

	@Enumerated(EnumType.STRING)
	@Column(name= "genre",nullable = false)
	private Genre genre;

	@Column (name="duration",nullable = false)
	private int duration;

	@OneToMany(mappedBy = "movie", cascade = CascadeType.ALL)
	private List<ScreeningsEntity> screeningsEntities;


	public MovieEntity(String title, String description, Genre genre, int duration) {
		this.title = title;
		this.description = description;
		this.genre = genre;
		this.duration = duration;
		this.screeningsEntities = new ArrayList<>();
	}
}
