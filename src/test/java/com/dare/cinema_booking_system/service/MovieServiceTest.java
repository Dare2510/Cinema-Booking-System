package com.dare.cinema_booking_system.service;


import com.dare.cinema_booking_system.movies.dto.MoviesRequest;
import com.dare.cinema_booking_system.movies.dto.MoviesResponse;
import com.dare.cinema_booking_system.movies.entity.Genre;
import com.dare.cinema_booking_system.movies.entity.MovieEntity;
import com.dare.cinema_booking_system.movies.exceptions.MovieNotFoundException;
import com.dare.cinema_booking_system.movies.repository.MoviesRepository;
import com.dare.cinema_booking_system.movies.service.MoviesService;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Optional;

import static org.mockito.Mockito.when;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
public class MovieServiceTest {

	@Mock
	private MoviesRepository  moviesRepository;
	@Spy
	private ModelMapper modelMapper;
	@InjectMocks
	private MoviesService moviesService;

	@Test
	public void addMovie_returnsMovieResponse(){
		MoviesRequest toAdd = new MoviesRequest("testTitle", "testDescription", 99, Genre.COMEDY);
		when(moviesRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

		MoviesResponse response = moviesService.addMovies(toAdd);

		assertEquals(toAdd.title, response.title);
		assertEquals(toAdd.description, response.description);
		assertEquals(toAdd.genre, response.genre);
		assertEquals(toAdd.duration, response.duration);
		verify(moviesRepository,times(1)).save(any());

	}

	@Test
	public void updateMovie_returnsMovieResponse(){

		MoviesRequest newInformation = new MoviesRequest("newTitle", "newDescription", 99, Genre.COMEDY);
		MovieEntity toUpdate = new MovieEntity("testTitle", "testDescription", Genre.COMEDY,99);
		Long movieId = toUpdate.getId();

		when(moviesRepository.findById(movieId)).thenReturn(Optional.of(toUpdate));

		moviesService.updateMovies(movieId, newInformation);

		verify(moviesRepository,times(1)).findById(movieId);
		verify(moviesRepository,times(1)).save(any());

		assertEquals("newTitle", toUpdate.getTitle());
		assertEquals("newDescription", toUpdate.getDescription());

	}

	@Test
	public void findMovieByID_returnsMovieNotFoundException(){
		assertThatThrownBy( () -> moviesService.getMovieResponseById(20L))
				.isInstanceOf(MovieNotFoundException.class)
				.hasMessage("Could not find movie with id: 20");
	}

	@Test
	public void deleteMovie_successful(){
		MovieEntity toDelete = new MovieEntity("testTitle", "testDescription", Genre.COMEDY,99);
		Long movieId = toDelete.getId();

		when(moviesRepository.findById(movieId)).thenReturn(Optional.of(toDelete));

		moviesService.deleteMovies(movieId);
		verify(moviesRepository,times(1)).findById(movieId);
		verify(moviesRepository,times(1)).delete(toDelete);
	}



}
