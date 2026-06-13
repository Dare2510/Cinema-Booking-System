package com.dare.cinema_booking_system.security.controller;

import com.dare.cinema_booking_system.security.dto.AuthRequest;
import com.dare.cinema_booking_system.security.entity.Role;
import com.dare.cinema_booking_system.security.service.AuthService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@AllArgsConstructor
public class AuthController {

	private final AuthService authService;

	@PostMapping("/login")
	public ResponseEntity<String> login(@RequestBody AuthRequest authRequest) {
		return ResponseEntity.ok().body(authService.login(authRequest.getEmail(), authRequest.getPassword()));
	}

	@PostMapping("/register")
	public ResponseEntity<String> registerUser(@RequestBody AuthRequest authRequest) {
		authService.register(authRequest);
		return ResponseEntity.ok().body("Register successful");
	}

	@PreAuthorize("hasRole('ADMIN')")
	@PostMapping("/register/{role}")
	public ResponseEntity<String> registerManagement(@RequestBody AuthRequest authRequest, @PathVariable Role role) {
		authService.registerManagement(authRequest, role);
		return ResponseEntity.ok().body("Register successful");
	}
}
