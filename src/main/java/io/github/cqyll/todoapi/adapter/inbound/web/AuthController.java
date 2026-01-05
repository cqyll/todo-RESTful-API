package io.github.cqyll.todoapi.adapter.inbound.web;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import io.github.cqyll.todoapi.application.service.UserAuthenticationService;
import io.github.cqyll.todoapi.dto.LoginRequest;

public class AuthController implements HttpHandler {
	private final UserAuthenticationService authService;
	private static final ObjectMapper mapper = new ObjectMapper();

	public AuthController(UserAuthenticationService authService) {
		this.authService = authService;
	}

	@Override
	public void handle(HttpExchange exchange) throws IOException {

		if (!"POST".equals(exchange.getRequestMethod())) {
			exchange.sendResponseHeaders(405, -1);
			return;
		}

		LoginRequest request; // dto

		try (InputStream is = exchange.getRequestBody()) {
			request = mapper.readValue(is, LoginRequest.class);
		} catch (Exception e) {
			sendResponse(exchange, 400, "Invalid JSON");
			return;
		}

		if (request.getEmail() == null || request.getPassword() == null) { // refactor for OAuth
			sendResponse(exchange, 400, "Missing fields"); 
			return;
		}

		// calling service

		try {
			String issueValidToken = authService.login(
					request.getEmail(),
					request.getPassword());

			//success
			sendResponse(exchange, 200, issueValidToken);
		} catch (IllegalArgumentException e) {
			// invalid credentials --> 401 Unauthorized
			sendResponse(exchange, 401, e.getMessage());
		} catch (IllegalStateException e) {
			// account not active --> 403 forbidden (authenticated, but not allowed)
			sendResponse(exchange, 403, e.getMessage()); 
		}


	}

	private void sendResponse(HttpExchange exchange, int status, Object body)
			throws IOException {

		byte[] bytes = mapper.writeValueAsBytes(body);

		exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
		exchange.sendResponseHeaders(status, bytes.length);

		try (OutputStream os = exchange.getResponseBody()) {
			os.write(bytes);
		}


	}
}
