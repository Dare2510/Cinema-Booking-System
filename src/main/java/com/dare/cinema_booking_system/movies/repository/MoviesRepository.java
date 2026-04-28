package com.dare.cinema_booking_system.movies.repository;

import com.dare.cinema_booking_system.movies.entity.Genre;
import com.dare.cinema_booking_system.movies.entity.MovieEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MoviesRepository extends JpaRepository<MovieEntity,Long> {

	MovieEntity findByMovieId(long movieId);
	List<MovieEntity> findByGenre(Genre genre);
	List<MovieEntity> findByDurationGreaterThanOrderDesc(int duration);

}
