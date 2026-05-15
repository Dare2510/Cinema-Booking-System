package com.dare.cinema_booking_system.movies.service;

import com.dare.cinema_booking_system.movies.dto.MovieRequest;
import com.dare.cinema_booking_system.movies.dto.MovieResponse;
import com.dare.cinema_booking_system.movies.entity.Genre;
import com.dare.cinema_booking_system.movies.entity.MovieEntity;
import com.dare.cinema_booking_system.movies.exceptions.MovieByDurationNotFoundException;
import com.dare.cinema_booking_system.movies.exceptions.MovieByGenreNotFoundException;
import com.dare.cinema_booking_system.movies.exceptions.MovieNotFoundException;
import com.dare.cinema_booking_system.movies.repository.MovieRepository;
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

	public MovieResponse getMovieResponseById(Long id) {
		MovieEntity toFind = getMovieEntityById(id);

		log.info("Found movie with ID {}", toFind.getId());
		return mappingResponse(toFind);
	}

	public List<MovieResponse> getListOfByGenre(Genre genre) {
		List<MovieEntity> listOfMovies = movieRepository.findByGenre(genre)
				.filter(list -> !list.isEmpty())
				.orElseThrow(
						() -> {
							log.info("No movies found for genre {}", genre);
							return new MovieByGenreNotFoundException(genre);
						}
				);

		return mappingListOfResponses(listOfMovies);
	}

	public List<MovieResponse> getListOfByDuration(int duration) {
		List<MovieEntity> listOfMovies = movieRepository.findByDurationGreaterThan(duration)
				.filter(list -> !list.isEmpty())
				.orElseThrow(
				() -> {
					log.warn("No movies found with duration greater than {} min", duration);
					return new MovieByDurationNotFoundException(duration);
				}
		);
		return mappingListOfResponses(listOfMovies);
	}

	public MovieResponse addMovies(MovieRequest movieRequest) {
		MovieEntity newMovie = modelMapper.map(movieRequest, MovieEntity.class);

		newMovie = movieRepository.save(newMovie);

		log.info("Added movie with ID {}", newMovie.getId());
		return mappingResponse(newMovie);
	}

	public MovieResponse updateMovies(Long id, MovieRequest movieRequest) {
		MovieEntity toUpdate = getMovieEntityById(id);

		modelMapper.map(movieRequest, toUpdate);
		movieRepository.save(toUpdate);

		log.info("Updated movie with ID {}", toUpdate.getId());
		return mappingResponse(toUpdate);
	}

	public void deleteMovies(Long id) {
		MovieEntity movieEntity = getMovieEntityById(id);

		movieRepository.delete(movieEntity);
		log.info("Deleted movie with ID {}", movieEntity.getId());
	}
	//Helper Methods

	public MovieEntity getMovieEntityById(Long id) {
		return movieRepository.findById(id)
				.orElseThrow(() -> {
					log.error("Movie with ID {} was not found", id);
					return new MovieNotFoundException(id);
				});
	}

	public List<MovieResponse> mappingListOfResponses(List<MovieEntity> listOfMovies) {
		return listOfMovies.stream().map(movie -> modelMapper.map(movie, MovieResponse.class))
				.toList();
	}

	public MovieResponse mappingResponse(MovieEntity movieEntity) {
		return modelMapper.map(movieEntity, MovieResponse.class);
	}


}
