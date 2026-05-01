package com.dare.cinema_booking_system.movies.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "Movies")
@Data
@NoArgsConstructor
public class MovieEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String title;

	@Column(nullable = false)
	private String description;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private Genre genre;

	@Column (nullable = false)
	private int duration;
}
