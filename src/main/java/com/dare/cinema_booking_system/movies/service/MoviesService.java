package com.dare.cinema_booking_system.movies.service;

import com.dare.cinema_booking_system.movies.dto.MoviesRequest;
import com.dare.cinema_booking_system.movies.dto.MoviesResponse;
import com.dare.cinema_booking_system.movies.entity.MovieEntity;
import com.dare.cinema_booking_system.movies.exceptions.MovieNotFoundException;
import com.dare.cinema_booking_system.movies.repository.MoviesRepository;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;


@Service
@AllArgsConstructor
public class MoviesService {

	private final MoviesRepository moviesRepository;
	private final ObjectMapper objectMapper;
	private final ModelMapper modelMapper;


	public Page<MoviesResponse> getPageOfMovies(Pageable pageable) {
		return moviesRepository.findAll(pageable)
				.map(movies -> modelMapper.map(movies, MoviesResponse.class));
	}

	public MoviesResponse getMoviesById(Long id) {
		return  moviesRepository.findById(id)
				.map(movies -> modelMapper.map(movies, MoviesResponse.class))
				.orElseThrow(() -> new MovieNotFoundException(id));
	}

	public MoviesResponse addMovies(MoviesRequest moviesRequest) {
		MovieEntity movieEntity = modelMapper.map(moviesRequest, MovieEntity.class);

		movieEntity = moviesRepository.save(movieEntity);

		return modelMapper.map(movieEntity, MoviesResponse.class);

	}

	public MoviesResponse updateMovies(Long id, MoviesRequest moviesRequest) {
		MovieEntity movieEntity = modelMapper.map(getMoviesById(id), MovieEntity.class);

		moviesRepository.save(movieEntity);

		return modelMapper.map(movieEntity, MoviesResponse.class);
	}

	public MoviesResponse deleteMovies(Long id) {
		MovieEntity movieEntity = modelMapper.map(getMoviesById(id), MovieEntity.class);

		moviesRepository.delete(movieEntity);

		return modelMapper.map(movieEntity, MoviesResponse.class);
	}
}
