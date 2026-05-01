package com.dare.cinema_booking_system.movies.repository;

import com.dare.cinema_booking_system.movies.entity.Genre;
import com.dare.cinema_booking_system.movies.entity.MovieEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MoviesRepository extends JpaRepository<MovieEntity,Long> {

	Optional<List<MovieEntity>> findByGenre(Genre genre);
	Optional<List<MovieEntity>> findByDurationGreaterThan(int duration);

}
