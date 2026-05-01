package com.dare.cinema_booking_system.movies.repository;

import com.dare.cinema_booking_system.movies.entity.Genre;
import com.dare.cinema_booking_system.movies.entity.MovieEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MoviesRepository extends JpaRepository<MovieEntity,Long> {

	MovieEntity findById(long Id);
	List<MovieEntity> findByGenre(Genre genre);
	List<MovieEntity> findByDurationGreaterThan(int duration);

}
