package com.dare.cinema_booking_system.security.jwt;

import com.dare.cinema_booking_system.user.entity.Role;
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
	@Value("${jwt.expiration-ms}")
	Long expirationMs;

	private Key getSigningKey() {
		return Keys.hmacShaKeyFor(secret.getBytes());
	}

	public String generateToken(String email, Role role, Long userId) {
		return Jwts.builder()
				.subject(email)
				.claim("role", role.name())
				.claim("userId", userId)
				.issuedAt(new Date(System.currentTimeMillis()))
				.expiration(new Date(System.currentTimeMillis() + expirationMs))
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

	public Long extractUserId(String token) {
		return Long.valueOf(
				Jwts.parser()
						.setSigningKey(getSigningKey())
						.build()
						.parseSignedClaims(token)
						.getPayload()
						.get("userId")
						.toString());
	}

}
