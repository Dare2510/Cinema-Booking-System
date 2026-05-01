package com.dare.cinema_booking_system.movies.service;

import com.dare.cinema_booking_system.movies.dto.MoviesRequest;
import com.dare.cinema_booking_system.movies.dto.MoviesResponse;
import com.dare.cinema_booking_system.movies.entity.MovieEntity;
import com.dare.cinema_booking_system.movies.exceptions.MovieNotFoundException;
import com.dare.cinema_booking_system.movies.repository.MoviesRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;


@Service
@AllArgsConstructor
@Slf4j
public class MoviesService {

	private final MoviesRepository moviesRepository;
	private final ModelMapper modelMapper;


	public Page<MoviesResponse> getPageOfMovies(Pageable pageable) {
		return moviesRepository.findAll(pageable)
				.map(movies -> modelMapper.map(movies, MoviesResponse.class));
	}

	public MoviesResponse findMovieByID(Long id) {
		MovieEntity toFind = getMoviesById(id);

		log.info("Found movie with ID {}", toFind.getId());

		return getMovieResponse(toFind);
	}

	public MoviesResponse addMovies(MoviesRequest moviesRequest) {
		MovieEntity movieEntity = modelMapper.map(moviesRequest, MovieEntity.class);

		movieEntity = moviesRepository.save(movieEntity);

		log.info("Added movie with ID {}", movieEntity.getId());
		return getMovieResponse(movieEntity);

	}

	public MoviesResponse updateMovies(Long id, MoviesRequest moviesRequest) {
		MovieEntity toUpdate = getMoviesById(id);

		modelMapper.map(moviesRequest, toUpdate);
		moviesRepository.save(toUpdate);

		log.info("Updated movie with ID {}", toUpdate.getId());

		return getMovieResponse(toUpdate);
	}

	public void deleteMovies(Long id) {
		MovieEntity movieEntity = getMoviesById(id);

		moviesRepository.delete(movieEntity);

		log.info("Deleted movie with ID {}", movieEntity.getId());

	}
	//Helper Methods

	public MovieEntity getMoviesById(Long id) {
		return moviesRepository.findById(id)
				.orElseThrow(() -> {
					log.error("Movie with ID {} was not found", id);
					return new MovieNotFoundException(id);
				});
	}

	public MoviesResponse getMovieResponse(MovieEntity movieEntity) {
		return modelMapper.map(movieEntity, MoviesResponse.class);
	}
}
