package io.github.cqyll.todoapi.domain;

import java.util.Objects;
import java.util.UUID;

import io.github.cqyll.todoapi.application.port.outbound.PasswordHasherPort;

/**
 * User Entity
 * - Has identity (ID)
 * - Can change state over time
 * - Equal by identity (ID), not values 
 */


public class User {
	private final UUID id;
	private final String name;
	private final String email;
	private Password password; // value object, can be null for OAuth users
	private boolean active;
	private boolean emailVerified;



	private User(UUID id, String name, String email) {
		validateEmail(email);
		
		this.id = id;
		this.name = name != null ? name.trim() : "";
		this.email = email.trim().toLowerCase();
		this.active = true;
		this.emailVerified = false;
	}


	// factory for registration via pw
	public static User createWithPassword(String email, String name, Password password) {
		User user = new User(UUID.randomUUID(), name, email);
		user.password = password;
		
		//handle email verification?

		return user;
	}

	// factory for registration via OAuth
	public static User createOAuthUser(String email, String name) {
		User user = new User(UUID.randomUUID(), email, name);
		user.emailVerified = true; // providers verify

		return user;
	}
	
	public boolean verifyPassword(String rawPassword, PasswordHasherPort hasher) {
		if (password == null) {
			return false; // not possible for OAuth users
		}
		return password.matches(rawPassword, hasher);
	}


	private void validateEmail(String email) {
		if (email == null || email.isBlank()) {
			throw new IllegalArgumentException("Email cannot be empty");
		}
		if (!email.contains("@")) {
			throw new IllegalArgumentException("Invalid email format");
		}
	}


	// change password?


	// getters
	public UUID getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getEmail() {
		return email;
	}

	Password getPassword() {
		return password;
	}

	public boolean isActive() {
		return active;
	}

	public boolean isEmailVerified() {
		return emailVerified;
	}

	@Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        User user = (User) obj;
        return id.equals(user.id); // equal by id
    }
	
	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

}
