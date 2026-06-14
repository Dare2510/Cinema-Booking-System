package com.dare.cinema_booking_system.user.controller;

import com.dare.cinema_booking_system.user.dto.UserRequest;
import com.dare.cinema_booking_system.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor

public class UserController {

	private final UserService userService;

	@PostMapping("/register")
	public ResponseEntity<String> registerUser(@RequestBody @Valid UserRequest userRequest) {
		userService.registerUser(userRequest);
		return ResponseEntity.ok().body("Register successful");
	}

}