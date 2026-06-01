package com.dare.cinema_booking_system.movie.service;

import com.dare.cinema_booking_system.movie.dto.MovieRequest;
import com.dare.cinema_booking_system.movie.dto.MovieResponse;
import com.dare.cinema_booking_system.movie.entity.Genre;
import com.dare.cinema_booking_system.movie.entity.MovieEntity;
import com.dare.cinema_booking_system.movie.exceptions.MovieByDurationNotFoundException;
import com.dare.cinema_booking_system.movie.exceptions.MovieByGenreNotFoundException;
import com.dare.cinema_booking_system.movie.exceptions.MovieNotFoundException;
import com.dare.cinema_booking_system.movie.repository.MovieRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@AllArgsConstructor
@Slf4j
public class MovieService {

	private final MovieRepository movieRepository;
	private final ModelMapper modelMapper;

	public Page<MovieResponse> getPageOfMovies(Pageable pageable) {
		return movieRepository.findAll(pageable)
				.map(movies ->
				{
					log.info("Getting Page of Movies");
					return modelMapper.map(movies, MovieResponse.class);
				});
	}

	public MovieResponse getMovieResponseById(Long movieId) {
		MovieEntity toFind = getMovieEntityById(movieId);

		log.info("Found movie with ID {}", toFind.getId());
		return mappingResponse(toFind);
	}

	public List<MovieResponse> getListOfByGenre(Genre genre) {
		List<MovieEntity> listOfMovies = movieRepository.findByGenre(genre);
		if (!listOfMovies.isEmpty()) {
			return mappingListOfResponses(listOfMovies);
		} else {
			log.warn("No movies found for genre {}", genre);
			throw new MovieByGenreNotFoundException(genre);
		}
	}

	public List<MovieResponse> getListOfByDuration(int duration) {
		List<MovieEntity> listOfMovies = movieRepository.findByDurationGreaterThan(duration);
		if (!listOfMovies.isEmpty()) {
			return mappingListOfResponses(listOfMovies);
		} else {
			log.warn("No movies found with duration greater than {} min", duration);
			throw new MovieByDurationNotFoundException(duration);
		}
	}

	public MovieResponse addMovies(MovieRequest movieRequest) {
		MovieEntity newMovie = modelMapper.map(movieRequest, MovieEntity.class);

		newMovie = movieRepository.save(newMovie);

		log.info("Added movie with ID {}", newMovie.getId());
		return mappingResponse(newMovie);
	}

	public MovieResponse updateMovies(Long movieId, MovieRequest movieRequest) {
		MovieEntity toUpdate = getMovieEntityById(movieId);

		modelMapper.map(movieRequest, toUpdate);
		movieRepository.save(toUpdate);

		log.info("Updated movie with ID {}", toUpdate.getId());
		return mappingResponse(toUpdate);
	}

	public void deleteMovies(Long movieId) {
		MovieEntity movieEntity = getMovieEntityById(movieId);

		movieRepository.delete(movieEntity);
		log.info("Deleted movie with ID {}", movieEntity.getId());
	}
	//Helper Methods

	public MovieEntity getMovieEntityById(Long movieId) {
		return movieRepository.findById(movieId)
				.orElseThrow(() -> {
					log.warn("Movie with ID {} was not found", movieId);
					return new MovieNotFoundException(movieId);
				});
	}

	private List<MovieResponse> mappingListOfResponses(List<MovieEntity> listOfMovies) {
		return listOfMovies.stream().map(movie -> modelMapper.map(movie, MovieResponse.class))
				.toList();
	}

	private MovieResponse mappingResponse(MovieEntity movieEntity) {
		return modelMapper.map(movieEntity, MovieResponse.class);
	}


}
