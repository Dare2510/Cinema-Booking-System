package com.dare.cinema_booking_system.service;

import com.dare.cinema_booking_system.movie.dto.MovieRequest;
import com.dare.cinema_booking_system.movie.dto.MovieResponse;
import com.dare.cinema_booking_system.movie.entity.Genre;
import com.dare.cinema_booking_system.movie.entity.MovieEntity;
import com.dare.cinema_booking_system.movie.exceptions.*;
import com.dare.cinema_booking_system.movie.repository.MovieRepository;
import com.dare.cinema_booking_system.movie.service.MovieService;
import com.dare.cinema_booking_system.screenings.repository.ScreeningRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MovieServiceTest {

	private static final Long MOVIE_ID = 1L;

	private static final String TITLE = "testTitle";
	private static final String DESCRIPTION = "testDescription";
	private static final int DURATION = 99;
	private static final Genre GENRE = Genre.COMEDY;

	@Mock
	private MovieRepository movieRepository;

	@Mock
	private ScreeningRepository screeningRepository;

	@Spy
	private ModelMapper modelMapper;

	@InjectMocks
	private MovieService movieService;

	@Test
	void addMovie_whenSuccessful_returnsMovieResponse() {
		MovieRequest request = movieRequest();

		when(movieRepository.save(any(MovieEntity.class)))
				.thenAnswer(invocation -> {
					MovieEntity movie = invocation.getArgument(0);
					movie.setId(MOVIE_ID);
					return movie;
				});

		MovieResponse response = movieService.addMovie(request);

		assertMovieResponse(response, TITLE, DESCRIPTION, DURATION, GENRE);

		verify(movieRepository).save(any(MovieEntity.class));
	}

	@Test
	void updateMovie_whenMovieIsFoundAndNoScreeningExists_returnsMovieResponse() {
		MovieEntity movie = movieEntity();
		MovieRequest request = movieRequest("newTitle", "newDescription", 120, Genre.DRAMA);

		when(movieRepository.findById(MOVIE_ID)).thenReturn(Optional.of(movie));
		when(screeningRepository.existsByMovieId(MOVIE_ID)).thenReturn(false);
		when(movieRepository.save(any(MovieEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

		MovieResponse response = movieService.updateMovie(MOVIE_ID, request);

		assertMovieResponse(response, "newTitle", "newDescription", 120, Genre.DRAMA);
		assertMovieEntity(movie, "newTitle", "newDescription", 120, Genre.DRAMA);

		verify(movieRepository).findById(MOVIE_ID);
		verify(screeningRepository).existsByMovieId(MOVIE_ID);
		verify(movieRepository).save(movie);
	}

	@Test
	void updateMovie_whenMovieIsFoundAndScreeningExists_throwsMovieUpdateNotPossibleException() {
		MovieEntity movie = movieEntity();
		MovieRequest request = movieRequest("newTitle", "newDescription", 120, Genre.DRAMA);

		when(movieRepository.findById(MOVIE_ID)).thenReturn(Optional.of(movie));
		when(screeningRepository.existsByMovieId(MOVIE_ID)).thenReturn(true);

		assertThatThrownBy(() -> movieService.updateMovie(MOVIE_ID, request))
				.isInstanceOf(MovieUpdateNotPossibleException.class)
				.hasMessage("Movie with id " + MOVIE_ID + " cannot be updated, screening exits");

		assertMovieEntity(movie, TITLE, DESCRIPTION, DURATION, GENRE);

		verify(movieRepository).findById(MOVIE_ID);
		verify(screeningRepository).existsByMovieId(MOVIE_ID);
		verify(movieRepository, never()).save(any(MovieEntity.class));
	}

	@Test
	void getMovieResponseById_whenMovieExists_returnsMovieResponse() {
		MovieEntity movie = movieEntity();

		when(movieRepository.findById(MOVIE_ID)).thenReturn(Optional.of(movie));

		MovieResponse response = movieService.getMovieResponseById(MOVIE_ID);

		assertMovieResponse(response, TITLE, DESCRIPTION, DURATION, GENRE);

		verify(movieRepository).findById(MOVIE_ID);

	}

	@Test
	void getMovieResponseById_whenMovieIsNotFound_throwsMovieNotFoundException() {
		when(movieRepository.findById(MOVIE_ID)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> movieService.getMovieResponseById(MOVIE_ID))
				.isInstanceOf(MovieNotFoundException.class)
				.hasMessage("Could not find movie with id: " + MOVIE_ID);

		verify(movieRepository).findById(MOVIE_ID);

	}

	@Test
	void getMovieEntityById_whenMovieExists_returnsMovieEntity() {
		MovieEntity movie = movieEntity();

		when(movieRepository.findById(MOVIE_ID)).thenReturn(Optional.of(movie));

		MovieEntity result = movieService.getMovieEntityById(MOVIE_ID);

		assertSame(movie, result);

		verify(movieRepository).findById(MOVIE_ID);

	}

	@Test
	void getMovieEntityById_whenMovieIsNotFound_throwsMovieNotFoundException() {
		when(movieRepository.findById(MOVIE_ID)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> movieService.getMovieEntityById(MOVIE_ID))
				.isInstanceOf(MovieNotFoundException.class)
				.hasMessage("Could not find movie with id: " + MOVIE_ID);

		verify(movieRepository).findById(MOVIE_ID);

	}

	@Test
	void deleteMovie_whenMovieIsFoundAndNoScreeningExists_deletesMovie() {
		MovieEntity movie = movieEntity();

		when(movieRepository.findById(MOVIE_ID)).thenReturn(Optional.of(movie));
		when(screeningRepository.existsByMovieId(MOVIE_ID)).thenReturn(false);

		movieService.deleteMovie(MOVIE_ID);

		verify(movieRepository).findById(MOVIE_ID);
		verify(screeningRepository).existsByMovieId(MOVIE_ID);
		verify(movieRepository).delete(movie);
	}

	@Test
	void deleteMovie_whenMovieIsFoundAndScreeningExists_throwsMovieDeletionNotPossibleException() {
		MovieEntity movie = movieEntity();

		when(movieRepository.findById(MOVIE_ID)).thenReturn(Optional.of(movie));
		when(screeningRepository.existsByMovieId(MOVIE_ID)).thenReturn(true);

		assertThatThrownBy(() -> movieService.deleteMovie(MOVIE_ID))
				.isInstanceOf(MovieDeletionNotPossibleException.class)
				.hasMessage("Movie with id " + MOVIE_ID + " cannot be deleted, screening exits");

		verify(movieRepository).findById(MOVIE_ID);
		verify(screeningRepository).existsByMovieId(MOVIE_ID);
		verify(movieRepository, never()).delete(any(MovieEntity.class));
	}

	@Test
	void deleteMovie_whenMovieIsNotFound_throwsMovieNotFoundException() {
		when(movieRepository.findById(MOVIE_ID)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> movieService.deleteMovie(MOVIE_ID))
				.isInstanceOf(MovieNotFoundException.class)
				.hasMessage("Could not find movie with id: " + MOVIE_ID);

		verify(movieRepository).findById(MOVIE_ID);
		verify(movieRepository, never()).delete(any(MovieEntity.class));
	}

	@Test
	void getListOfByDuration_whenMoviesWithWantedDurationExist_returnsListOfMovieResponses() {
		List<MovieEntity> movies = movies();

		when(movieRepository.findByDurationGreaterThan(60)).thenReturn(movies);

		List<MovieResponse> responses = movieService.getListOfByDuration(60);

		assertEquals(2, responses.size());
		assertMovieResponse(responses.get(0), "testTitle1", "testDescription1", 99, Genre.COMEDY);
		assertMovieResponse(responses.get(1), "testTitle2", "testDescription2", 120, Genre.DRAMA);

		verify(movieRepository).findByDurationGreaterThan(60);
	}

	@Test
	void getListOfByDuration_whenNoMoviesWithWantedDurationWereFound_throwsMovieByDurationNotFoundException() {
		when(movieRepository.findByDurationGreaterThan(120)).thenReturn(List.of());

		assertThatThrownBy(() -> movieService.getListOfByDuration(120))
				.isInstanceOf(MovieByDurationNotFoundException.class)
				.hasMessage("No movies with greater than 120 min duration found");

		verify(movieRepository).findByDurationGreaterThan(120);
	}

	@Test
	void getListOfByGenre_whenMoviesWithWantedGenreWereFound_returnsListOfMovieResponses() {
		List<MovieEntity> movies = List.of(
				movieEntity(1L, "testTitle1", "testDescription1", 99, Genre.FANTASY),
				movieEntity(2L, "testTitle2", "testDescription2", 120, Genre.FANTASY)
		);

		when(movieRepository.findByGenre(Genre.FANTASY)).thenReturn(movies);

		List<MovieResponse> responses = movieService.getListOfByGenre(Genre.FANTASY);

		assertEquals(2, responses.size());
		assertMovieResponse(responses.get(0), "testTitle1", "testDescription1", 99, Genre.FANTASY);
		assertMovieResponse(responses.get(1), "testTitle2", "testDescription2", 120, Genre.FANTASY);

		verify(movieRepository).findByGenre(Genre.FANTASY);

	}

	@Test
	void getListOfByGenre_whenNoMoviesWithWantedGenreWereFound_throwsMovieByGenreNotFoundException() {
		when(movieRepository.findByGenre(Genre.FANTASY)).thenReturn(List.of());

		assertThatThrownBy(() -> movieService.getListOfByGenre(Genre.FANTASY))
				.isInstanceOf(MovieByGenreNotFoundException.class)
				.hasMessage("No movies with FANTASY as genre were found");

		verify(movieRepository).findByGenre(Genre.FANTASY);
	}

	private MovieRequest movieRequest() {
		return movieRequest(TITLE, DESCRIPTION, DURATION, GENRE);
	}

	private MovieRequest movieRequest(String title, String description, int duration, Genre genre) {
		return new MovieRequest(title, description, duration, genre);
	}

	private MovieEntity movieEntity() {
		return movieEntity(MOVIE_ID, TITLE, DESCRIPTION, DURATION, GENRE);
	}

	private MovieEntity movieEntity(Long id, String title, String description, int duration, Genre genre) {
		MovieEntity movie = new MovieEntity(title, description, genre, duration);
		movie.setId(id);
		return movie;
	}

	private List<MovieEntity> movies() {
		return List.of(
				movieEntity(1L, "testTitle1", "testDescription1", 99, Genre.COMEDY),
				movieEntity(2L, "testTitle2", "testDescription2", 120, Genre.DRAMA)
		);
	}

	private void assertMovieResponse(MovieResponse response, String title, String description, int duration, Genre genre) {
		assertNotNull(response);
		assertEquals(title, response.getTitle());
		assertEquals(description, response.getDescription());
		assertEquals(duration, response.getDuration());
		assertEquals(genre, response.getGenre());
	}

	private void assertMovieEntity(MovieEntity movie, String title, String description, int duration, Genre genre) {
		assertEquals(title, movie.getTitle());
		assertEquals(description, movie.getDescription());
		assertEquals(duration, movie.getDuration());
		assertEquals(genre, movie.getGenre());
	}
}
