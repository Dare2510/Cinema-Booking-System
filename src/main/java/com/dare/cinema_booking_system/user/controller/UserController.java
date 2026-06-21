package com.dare.cinema_booking_system.user.controller;

import com.dare.cinema_booking_system.security.principal.AuthenticatedUser;
import com.dare.cinema_booking_system.user.dto.UserRequest;
import com.dare.cinema_booking_system.user.dto.UserResponse;
import com.dare.cinema_booking_system.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor

public class UserController {

	private final UserService userService;

	@PostMapping("/register")
	public ResponseEntity<UserResponse> registerUser(@RequestBody @Valid UserRequest userRequest) {
		return ResponseEntity.ok().body(userService.registerUserByCustomer(userRequest));
	}

	@PatchMapping("/{password}/update")
	public ResponseEntity<Void> updateUser(@RequestBody @Valid UserRequest userRequest, @AuthenticationPrincipal AuthenticatedUser authenticatedUser, @PathVariable String password) {
		userService.updateUserByCustomer(authenticatedUser, userRequest, password);
		return ResponseEntity.ok().build();
	}

	@DeleteMapping("/{password}/delete")
	public ResponseEntity<Void> deleteUser(@AuthenticationPrincipal AuthenticatedUser authenticatedUser, @PathVariable String password) {
		userService.deleteUserByCustomer(authenticatedUser, password);
		return ResponseEntity.ok().build();
	}

}