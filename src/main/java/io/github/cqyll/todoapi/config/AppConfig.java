package io.github.cqyll.todoapi.config;

import io.github.cqyll.todoapi.adapter.inbound.web.UserController;
import io.github.cqyll.todoapi.adapter.inbound.web.AuthController;
import io.github.cqyll.todoapi.application.service.UserRegistrationService;
import io.github.cqyll.todoapi.application.service.UserAuthenticationService;
import io.github.cqyll.todoapi.application.port.outbound.UserRepositoryPort;
import io.github.cqyll.todoapi.application.port.outbound.PasswordHasherPort;
import io.github.cqyll.todoapi.application.port.outbound.TokenProviderPort;
import io.github.cqyll.todoapi.adapter.outbound.persistence.InMemoryUserAdapter;
import io.github.cqyll.todoapi.adapter.outbound.security.SimplePasswordHasherAdapter;
import io.github.cqyll.todoapi.adapter.outbound.security.FakeTokenProviderAdapter;

import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;

import com.sun.net.httpserver.HttpContext;


public class AppConfig {
	private UserRegistrationService userRegistrationService;
	private UserAuthenticationService userAuthenticationService;
	private UserController userController;
	private AuthController authController;

	public AppConfig() {
		initialize();
	}

	private void initialize() {
		// created adapters
		UserRepositoryPort userRepository = new InMemoryUserAdapter();
		PasswordHasherPort passwordHasher = new SimplePasswordHasherAdapter();
		TokenProviderPort tokenProvider = new FakeTokenProviderAdapter();

		// created services for each use case
		userRegistrationService = new UserRegistrationService(userRepository, passwordHasher, tokenProvider);
		// userAuthenticationService = new UserAuthenticationService(
		//    userRepository, passwordHasher, tokenProvider);

		// created corresponding controllers
		userController = new UserController(userRegistrationService);
		// authController = new AuthController(userAuthenticationService);

	}

	public HttpServer createHttpServer() {
		try {
			// creating socket address via hostname string may resolve to IPv6
			HttpServer server = HttpServer.create(
					new InetSocketAddress("localhost", 8080), 0);

			server.createContext("/register", userController);

			return server;
		} catch(IOException e) {
			throw new RuntimeException("Failed to create HTTP server", e);
		}
	}

	public UserController getUserController() { return userController; }
	public AuthController getAuthController() { return authController; }
}