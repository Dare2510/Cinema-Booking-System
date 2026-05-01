package com.dare.cinema_booking_system.movies.service;

import com.dare.cinema_booking_system.movies.dto.MoviesRequest;
import com.dare.cinema_booking_system.movies.dto.MoviesResponse;
import com.dare.cinema_booking_system.movies.entity.Genre;
import com.dare.cinema_booking_system.movies.entity.MovieEntity;
import com.dare.cinema_booking_system.movies.exceptions.MovieByDurationNotFoundException;
import com.dare.cinema_booking_system.movies.exceptions.MovieByGenreNotFoundException;
import com.dare.cinema_booking_system.movies.exceptions.MovieNotFoundException;
import com.dare.cinema_booking_system.movies.repository.MoviesRepository;
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
public class MoviesService {

	private final MoviesRepository moviesRepository;
	private final ModelMapper modelMapper;

	public Page<MoviesResponse> getPageOfMovies(Pageable pageable) {
		return moviesRepository.findAll(pageable)
				.map(movies ->
				{
					log.info("Getting Page of Movies");
					return modelMapper.map(movies, MoviesResponse.class);
				});
	}

	public MoviesResponse getMovieResponseById(Long id) {
		MovieEntity toFind = getMovieEntityById(id);

		log.info("Found movie with ID {}", toFind.getId());
		return mappingResponse(toFind);
	}

	List<MoviesResponse> getListOfByGenre(Genre genre) {
		List<MovieEntity> listOfMovies = moviesRepository.findByGenre(genre)
				.orElseThrow(
						() -> {
							log.info("No movies found for genre {}", genre);
							return new MovieByGenreNotFoundException(genre);
						}
				);

		return mappingListOfResponses(listOfMovies);
	}

	List<MoviesResponse> getListOfByDuration(int duration) {
		List<MovieEntity> listOfMovies = moviesRepository.findByDurationGreaterThan(duration)
				.orElseThrow(
				() -> {
					log.warn("No movies found with duration greater than {} min", duration);
					return new MovieByDurationNotFoundException(duration);
				}
		);
		return mappingListOfResponses(listOfMovies);
	}

	public MoviesResponse addMovies(MoviesRequest moviesRequest) {
		MovieEntity movieEntity = modelMapper.map(moviesRequest, MovieEntity.class);

		movieEntity = moviesRepository.save(movieEntity);

		log.info("Added movie with ID {}", movieEntity.getId());
		return mappingResponse(movieEntity);
	}

	public MoviesResponse updateMovies(Long id, MoviesRequest moviesRequest) {
		MovieEntity toUpdate = getMovieEntityById(id);

		modelMapper.map(moviesRequest, toUpdate);
		moviesRepository.save(toUpdate);

		log.info("Updated movie with ID {}", toUpdate.getId());
		return mappingResponse(toUpdate);
	}

	public void deleteMovies(Long id) {
		MovieEntity movieEntity = getMovieEntityById(id);

		moviesRepository.delete(movieEntity);
		log.info("Deleted movie with ID {}", movieEntity.getId());
	}
	//Helper Methods

	public MovieEntity getMovieEntityById(Long id) {
		return moviesRepository.findById(id)
				.orElseThrow(() -> {
					log.error("Movie with ID {} was not found", id);
					return new MovieNotFoundException(id);
				});
	}

	public List<MoviesResponse> mappingListOfResponses(List<MovieEntity> listOfMovies) {
		return listOfMovies.stream().map(movie -> modelMapper.map(movie,MoviesResponse.class))
				.toList();
	}

	public MoviesResponse mappingResponse(MovieEntity movieEntity) {
		return modelMapper.map(movieEntity, MoviesResponse.class);
	}


}
