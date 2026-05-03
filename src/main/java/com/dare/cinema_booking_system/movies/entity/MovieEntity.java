package com.dare.cinema_booking_system.movies.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "Movies")
@Getter @Setter
@NoArgsConstructor
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

	public MovieEntity(String title, String description, Genre genre, int duration) {
		this.title = title;
		this.description = description;
		this.genre = genre;
		this.duration = duration;
	}
}
