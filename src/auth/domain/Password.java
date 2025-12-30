package auth.domain;


import java.util.Objects;
/**
 * Value Object
 * - Immutable
 * - Self-validating
 * - No identity, equal by value 
 */

public class Password {
	private final String hash;
	
	private Password(String hash) {
		validate(hash);
		this.hash = hash;
	}
	
	public static Password fromHash(String hash) {
		return new Password(hash);
	}
	
	private void validate(String hash) {
		if (hash == null || hash.isBlank()) {
			throw new IllegalArgumentException("Password hash cannot be empty.");
		}
		if (hash.length() < 32) {
			throw new IllegalArgumentException("Invalid password hash format");
		}
	}
	
	public boolean matches(String rawPassword, auth.application.port.outbound.PasswordHasherPort hasher) {
		return hasher.matches(rawPassword, this.hash);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null || getClass() != obj.getClass()) return false;
		Password password = (Password) obj;
		return hash.equals(password.hash);
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(hash);
	}
	
	public String getHash() {
		return hash; // safe getter (immutable object)
	}
	
}
