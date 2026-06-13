package com.dare.cinema_booking_system.security.jwt;

import com.dare.cinema_booking_system.security.entity.Role;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {

	@Value("${jwt.secret}")
	String secret;

	private Key getSigningKey() {
		return Keys.hmacShaKeyFor(secret.getBytes());
	}

	public String generateToken(String email, Role role) {
		return Jwts.builder()
				.subject(email)
				.claim("role", role)
				.issuedAt(new Date(System.currentTimeMillis() + 1000 * 60 * 60))
				.signWith(getSigningKey())
				.compact();
	}

	public String extractEmail(String token) {
		return Jwts.parser()
				.setSigningKey(getSigningKey())
				.build()
				.parseSignedClaims(token)
				.getPayload()
				.getSubject();
	}

	public Role extractRole(String token) {
		return Role.valueOf(
				Jwts.parser()
						.setSigningKey(getSigningKey())
						.build()
						.parseSignedClaims(token)
						.getPayload()
						.get("role")
						.toString());
	}

}
