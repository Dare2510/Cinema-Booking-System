package com.dare.cinema_booking_system.common;

import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.ObjectMapper;

import java.time.Clock;

@Configuration
public class AppConfiguration {
	@Bean
	public ObjectMapper objectMapper() {return new ObjectMapper();}

	@Bean
	public ModelMapper modelMapper() {
		return new ModelMapper();
	}

	@Bean
	public Clock clock() {
		return Clock.systemDefaultZone();
	}
}
