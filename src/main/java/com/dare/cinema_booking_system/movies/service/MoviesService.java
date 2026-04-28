package com.dare.cinema_booking_system.movies.service;

import com.dare.cinema_booking_system.movies.repository.MoviesRepository;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

@Service
@AllArgsConstructor
public class MoviesService {

	private final MoviesRepository moviesRepository;
	private final ObjectMapper objectMapper;
	private final ModelMapper modelMapper;

}
