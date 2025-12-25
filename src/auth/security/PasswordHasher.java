package auth.security;

public interface PasswordHasher {
	
	/**
	 * Defines the contract for password hashing implementations.
	 * 
	 * DESIGN DECISION: This interface follows the Dependency Inversion Principle.
	 * - UserService depends on this abstraction rather than concrete implementations
	 * - Allows swapping hashing algorithms (SHA-256 → BCrypt) without changing UserService
	 * 
	 * @see UserService#UserService(UserRepository, PasswordHasher, AuthenticationStrategy)
	 */
	
	String hash(String rawPassword);
	boolean matches(String rawPassword, String hashedPassword);
}
