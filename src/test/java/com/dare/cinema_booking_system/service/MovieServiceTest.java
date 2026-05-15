package com.dare.cinema_booking_system.service;


import com.dare.cinema_booking_system.movies.dto.MovieRequest;
import com.dare.cinema_booking_system.movies.dto.MovieResponse;
import com.dare.cinema_booking_system.movies.entity.Genre;
import com.dare.cinema_booking_system.movies.entity.MovieEntity;
import com.dare.cinema_booking_system.movies.exceptions.MovieByDurationNotFoundException;
import com.dare.cinema_booking_system.movies.exceptions.MovieByGenreNotFoundException;
import com.dare.cinema_booking_system.movies.exceptions.MovieNotFoundException;
import com.dare.cinema_booking_system.movies.repository.MovieRepository;
import com.dare.cinema_booking_system.movies.service.MovieService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.when;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
public class MovieServiceTest {

	@Mock
	private MovieRepository movieRepository;
	@Spy
	private ModelMapper modelMapper;
	@InjectMocks
	private MovieService movieService;

	@Test
	public void addMovie_whenSuccessful_returnsMovieResponse() {
		MovieRequest toAdd = new MovieRequest("testTitle", "testDescription", 99, Genre.COMEDY);

		when(movieRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
		MovieResponse response = movieService.addMovies(toAdd);

		assertEquals(toAdd.title, response.title);
		assertEquals(toAdd.description, response.description);
		assertEquals(toAdd.genre, response.genre);
		assertEquals(toAdd.duration, response.duration);
		verify(movieRepository, times(1)).save(any());

	}

	@Test
	public void updateMovie_whenMovieIsFound_returnsMovieResponse() {

		MovieRequest newInformation = new MovieRequest("newTitle", "newDescription", 99, Genre.COMEDY);
		MovieEntity toUpdate = new MovieEntity("testTitle", "testDescription", Genre.COMEDY, 99);
		Long movieId = toUpdate.getId();

		when(movieRepository.findById(movieId)).thenReturn(Optional.of(toUpdate));

		movieService.updateMovies(movieId, newInformation);

		verify(movieRepository, times(1)).findById(movieId);
		verify(movieRepository, times(1)).save(any());

		assertEquals("newTitle", toUpdate.getTitle());
		assertEquals("newDescription", toUpdate.getDescription());

	}

	@Test
	public void getMovieEntityById_whenMovieIsNotFound_throwsMovieNotFoundException() {
		assertThatThrownBy(() -> movieService.getMovieResponseById(20L))
				.isInstanceOf(MovieNotFoundException.class)
				.hasMessage("Could not find movie with id: 20");
	}

	@Test
	public void deleteMovie_whenMovieIsFound_successful() {
		MovieEntity toDelete = new MovieEntity("testTitle", "testDescription", Genre.COMEDY, 99);
		Long movieId = toDelete.getId();

		when(movieRepository.findById(movieId)).thenReturn(Optional.of(toDelete));
		movieService.deleteMovies(movieId);

		verify(movieRepository, times(1)).findById(movieId);
		verify(movieRepository, times(1)).delete(toDelete);

		assertFalse(movieRepository.existsById(movieId));
	}

	@Test
	public void getListOfByDuration_whenMoviesWithWantedDurationExists_returnsListOfMovieResponse() {
		List<MovieEntity> listOfMovies = new ArrayList<>();
		listOfMovies.add(new MovieEntity("testTitle1", "testDescription1", Genre.COMEDY, 99));
		listOfMovies.add(new MovieEntity("testTitle2", "testDescription2", Genre.DRAMA, 120));

		when(movieRepository.findByDurationGreaterThan(60)).thenReturn(Optional.of(listOfMovies));

		List<MovieResponse> filteredList = movieService.getListOfByDuration(60);

		verify(movieRepository, times(1)).findByDurationGreaterThan(60);
		assertEquals("testTitle1", filteredList.get(0).title);
		assertEquals("testTitle2", filteredList.get(1).title);

	}

	@Test
	public void getListOfByDuration_whenNoMoviesWithWantedDurationWereFound_returnsMovieByDurationNotFoundException() {
		assertThatThrownBy(() -> movieService.getListOfByDuration(120))
				.isInstanceOf(MovieByDurationNotFoundException.class)
				.hasMessage("No movies with greater than 120 min duration found");

	}

	@Test
	public void getListOfByGenre_whenMoviesWithWantedGenreWereFound_returnsListOfMovieResponse() {
		List<MovieEntity> listOfMovies = new ArrayList<>();
		listOfMovies.add(new MovieEntity("testTitle1", "testDescription1", Genre.FANTASY, 99));
		listOfMovies.add(new MovieEntity("testTitle2", "testDescription2", Genre.FANTASY, 120));

		when(movieRepository.findByGenre(Genre.FANTASY)).thenReturn(Optional.of(listOfMovies));

		List<MovieResponse> filteredList = movieService.getListOfByGenre(Genre.FANTASY);

		verify(movieRepository, times(1)).findByGenre(Genre.FANTASY);
		assertEquals("testTitle1", filteredList.get(0).title);
		assertEquals("testTitle2", filteredList.get(1).title);

	}

	@Test
	public void getListOfByGenre_whenNoMoviesWithWantedGenreWereFound_throwsMovieByGenreNotFoundException() {
		assertThatThrownBy(() -> movieService.getListOfByGenre(Genre.FANTASY))
				.isInstanceOf(MovieByGenreNotFoundException.class)
				.hasMessage("No movies with FANTASY as genre were found");
	}


}
