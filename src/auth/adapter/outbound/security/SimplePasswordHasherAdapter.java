package auth.adapter.outbound.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import auth.application.port.outbound.PasswordHasherPort;

public class SimplePasswordHasherAdapter implements 
	PasswordHasherPort {
	
	/**
	 * TEMPORARY IMPLEMENTATION - SHA-256 hashing
	 * TODO: Replace with BCrypt (salted, slow hashing)
	 * 
	 * DESIGN NOTE: This class can be swapped with BCryptHasher without 
	 * modifying UserService due to PasswordHasher interface.
	 */
	
	public String hash(String rawPassword) {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256"); // replace w/ BCrypt -__-
			byte[] encodedHash = digest.digest(
					rawPassword.getBytes(StandardCharsets.UTF_8));
			return bytesToHex(encodedHash);
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException("Hashing algorithm not available.", e);
		}
	}
	
	public boolean matches(String rawPassword, String hashedPassword) {
		return hash(rawPassword).equals(hashedPassword);
	}
	
	private String bytesToHex(byte[] hash) {
		StringBuilder hexString = new StringBuilder(2 * hash.length);
		for (byte b : hash ) {
			hexString.append(String.format("%02x", b));
		}
		return hexString.toString();
	}
}
