package auth.security;

public interface PasswordHasher {
	
	/*
	 * implemented as an interface for now b/c;
	 * i. there is only one active hasher
	 * ii. hashing is not being chosen at run time
	 * 
	 * -- WILL CHANGE -- req. reading 'dependency inversion'  
	 */
	String hash(String rawPassword);
	boolean matches(String rawPassword, String hashedPassword);
}
