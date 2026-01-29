package io.github.cqyll.todoapi;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;
import io.github.cqyll.todoapi.config.AppConfig;
import org.junit.jupiter.api.*;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ApiFlowTest {

	private static final ObjectMapper MAPPER = new ObjectMapper();
	private static final HttpClient HTTP = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();

	private static final String CLIENT_ID = "todo-web";
	private static final String CLIENT_SECRET = "todo-secret";

	private HttpServer server;

	private String baseUrl;

	@BeforeAll
	void startServer() {
		server = new AppConfig().createHttpServer();
		server.start();

		baseUrl = "http://localhost:" + server.getAddress().getPort();
	}

	@AfterAll
	void stopServer() {
		if (server != null)
			server.stop(0);
	}

	/**
	 * Verifies server can bind to an ephemeral port, to avoid port conflicts.
	 */
	@Test
	void usesEphemeralPort() {
		assertTrue(server.getAddress().getPort() > 0);
	}

	/**
	 * Happy-path flow:
	 * <ol>
	 * 		<li>Register a new user.</li>
	 *  	<li>Assert both return a usable token string.</li>
	 *  	<li>Assert both tokens are equal for the same user (based on a deterministic token provider)</li>
	 * </ol>
	 * 
	 * <p>Token extraction is shape-agnostic (supports "token", "access_token", or raw JSON string).</p>
	 */
	@Test
	void register_then_login() throws Exception {
		String email = "user" + System.currentTimeMillis() + "@example.com";
		String password = "Passw0rd!";

		String registrationToken = register(email, "JUnit", password);

		assertNotNull(registrationToken);
		assertFalse(registrationToken.isBlank());
		assertTrue(registrationToken.length() >= 10);
		assertFalse(registrationToken.chars().anyMatch(Character::isWhitespace));

		String loginToken = login(email, password);

		assertNotNull(loginToken);
		assertFalse(loginToken.isBlank());
		assertTrue(loginToken.length() >= 10);
		assertFalse(loginToken.chars().anyMatch(Character::isWhitespace));

		// current token generation implementation is deterministic
		// any token provider adapter changes will affect the following assertion
		assertEquals(registrationToken, loginToken);
	}


	/**
	 * Verifies password grant works when the client authenticates via HTTP Basic
	 * 
	 * <p>This test exists to cover the controller branch that parses client credentials from the Authorization header.
	 */
	@Test
	void passwordGrantWithBasicAuth() throws Exception {
		String email = "user" + System.currentTimeMillis() + "@example.com";
		String password = "Passw0rd!";
		register(email, "OAuth", password);

		HttpResponse<String> r = requestTokenBasicClient(email, password);
		assertEquals(200, r.statusCode(), "Token request failed: HTTP " + r.statusCode() + " body=" + r.body());

		Map<String, Object> body = parseJsonObject(r.body());

		String tokenType = String.valueOf(body.get("token_type"));
		assertNotNull(tokenType);
		assertEquals("bearer", tokenType.toLowerCase(Locale.ROOT));

		String accessToken = String.valueOf(body.get("access_token"));
		assertNotNull(accessToken);
		assertFalse(accessToken.isBlank());
		assertTrue(accessToken.length() >= 10);
		assertFalse(accessToken.chars().anyMatch(Character::isWhitespace));
	}


	/**
	 * Verifies password grant works when the client 
	 */
	@Test
	void passwordGrantWithClientCredentialsInBody() throws Exception {
		String email = "user" + System.currentTimeMillis() + "@example.com";
		String password = "Passw0rd!";
		register(email, "OAuth", password);

		HttpResponse<String> r = requestTokenBodyClient(email, password);
		assertEquals(200, r.statusCode(), "Token request failed: HTTP " + r.statusCode() + " body=" + r.body());

		Map<String, Object> body = parseJsonObject(r.body());

		String tokenType = String.valueOf(body.get("token_type"));
		assertNotNull(tokenType);
		assertEquals("bearer", tokenType.toLowerCase(Locale.ROOT));

		String accessToken = String.valueOf(body.get("access_token"));
		assertNotNull(accessToken);
		assertFalse(accessToken.isBlank());
		assertTrue(accessToken.length() >= 10);
		assertFalse(accessToken.chars().anyMatch(Character::isWhitespace));
	}


	private String login(String email, String password) throws
	Exception {
		Map<String, Object> payload = new LinkedHashMap<>();
		payload.put("email", email);
		payload.put("password", password);

		HttpResponse<String> r = sendJsonPost(baseUrl + "/login", payload);
		assertEquals(200, r.statusCode(), "Login failed: " + r.body());

		return extractToken(r.body());
	}

	/**
	 * Registers a new user via {@code POST /register}
	 * 
	 * <p>This is a happy-path helper: expects the request to succeed
	 * If the response status code is {@code >= 400}, throws exception.
	 * 
	 * <p>The response body is passed to {@link #extractToken(String)},
	 * which intentionally absorbs parsing or format errors and attempts to
	 * extract a token from multiple possible response shapes.
	 * 
	 * <p>This way the test harness remains resilient while the registration
	 * endpoint's response format evolves.
	 * 
	 * @param email user email address
	 * @param name user display name
	 * @param password plaintext password
	 * @return an authentication token issued after registration
	 * @throws Exception if the HTTP request fails or returns an error status
	 */
	private String register(String email, String name, String password)
			throws Exception {
		Map<String, Object> payload = new LinkedHashMap<>();
		payload.put("email", email);
		payload.put("name", name);
		payload.put("password", password);

		HttpResponse<String> r = sendJsonPost(baseUrl + "/register", payload);
		printResponse(r);

		if (r.statusCode() >= 400) {
			throw new RuntimeException("Register failed: " + r.body());
		}

		return extractToken(r.body());
	}




	/**
	 * Requests an access token using client authentication via HTTP Basic.
	 * 
	 * <p>Client credentials are provided via the Authorization header; user credentials are provided in the form body.</p>
	 */
	private HttpResponse<String> requestTokenBasicClient(String username, String password) throws Exception {
		String form = form(Map.of(
				"grant_type", "password",
				"username", username,
				"password", password));
		String basic = Base64.getEncoder().encodeToString((CLIENT_ID + ":" + CLIENT_SECRET).getBytes(StandardCharsets.UTF_8));

		HttpRequest req = HttpRequest.newBuilder()
				.uri(URI.create(baseUrl + "/oauth/token"))
				.header("Authorization", "Basic " + basic)
				.header("Content-Type", "application/x-www-form-urlencoded")
				.POST(HttpRequest.BodyPublishers.ofString(form))
				.build();
		return HTTP.send(req, HttpResponse.BodyHandlers.ofString());
	}

	/**
	 * Requests an access token using client credentials supplied in the request body.
	 * 
	 * <p>Client credentials are included as form parameters; user credentials are also provided in the same form body.</p>
	 */
	private HttpResponse<String> requestTokenBodyClient(String username, String password) throws Exception {
		String form = form(Map.of(
				"grant_type", "password",
				"client_id", CLIENT_ID,
				"client_secret", CLIENT_SECRET,
				"username", username,
				"password", password));

		HttpRequest req = HttpRequest.newBuilder()
				.uri(URI.create(baseUrl + "/oauth/token"))
				.header("Content-Type", "application/x-www-form-urlencoded")
				.POST(HttpRequest.BodyPublishers.ofString(form))
				.build();
		return HTTP.send(req, HttpResponse.BodyHandlers.ofString());
	}

	private HttpResponse<String> requestTokenInvalidClient(String username, String password) throws Exception {
		
		
		HttpRequest req = HttpRequest.newBuilder()
				.uri(URI.create(baseUrl + "/oauth/token"))
				.header("Content-Type", "application/json")
				.POST(HttpRequest.BodyPublishers.ofString())
				.build();
	}

	// Non-static by design: I/O helper and is considered part of the test instance lifecycle
	/**
	 * Sends an HTTP POST request with a JSON body.
	 * 
	 * <p>This is the base helper for all JSON-based endpoints such as
	 * {@code /register} and {@code /login}. The payload object is serialized 
	 * using Jackson and sent with {@code Content-Type: application/json}.
	 * 
	 * <p>This method does NOT assert on the response status code.
	 * Callers are responsible for validating success or failure.
	 * 
	 * @param url the full endpoint URL
	 * @param payload a Java object or Map to serialize as JSON
	 * @return the raw HTTP response containing status code and body
	 * @throws Exception
	 */
	private HttpResponse<String> sendJsonPost(String url, Object payload) throws Exception {
		String json = MAPPER.writeValueAsString(payload);
		HttpRequest req = HttpRequest.newBuilder().uri(URI.create(url))
				.header("Content-Type", "application/json; charset=utf-8")
				.POST(HttpRequest.BodyPublishers.ofString(json)).build();
		return HTTP.send(req, HttpResponse.BodyHandlers.ofString());
	}

	private static String form(Map<String, String> params) {
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		for (Map.Entry<String, String> e: params.entrySet()) {
			if (!first) sb.append('&');
			first = false;
			sb.append(enc(e.getKey())).append('=').append(enc(e.getValue()));
		}
		return sb.toString();
	}

	private static String enc(String s) {
		return URLEncoder.encode(s == null ? "": s, StandardCharsets.UTF_8);
	}

	/**
	 * Prints HTTP response status and body to standard output.
	 * 
	 * <p>This is a debugging aid used to inspect raw responses during tests.
	 * 
	 * @param response the HTTP response to print
	 */
	private static void printResponse(HttpResponse<String> response) {
		System.out.println("HTTP " + response.statusCode());
		if (response.body() == null || response.body().isBlank()) {
			System.out.println("(empty body)");
		} else {
			System.out.println(response.body());
		}
	}

	/**
	 * Extracts an authentication token from a response body, regardless
	 * of response shape.
	 * 
	 * <p>This helper supports multiple token formats:
	 * <ul>
	 * 		<li>{@code { "token": "..."}}</li>
	 * 		<li>{@code { "access_token": "..."}}</li>
	 * 		<li>{@code { "raw-token-string"}}</li>
	 * </ul>
	 * 
	 * <p>This exists because different endpoints in the system return
	 * tokens in different formats, and the test should remain resilient
	 * while API evolves.
	 * 
	 * @param body the raw response body
	 * @return the extracted token, or the raw body if no known format matches
	 */
	private static String extractToken(String body) {
		if (body == null)
			return null;

		try {
			Map<String, Object> obj = MAPPER.readValue(body, new TypeReference<Map<String, Object>>() {});
			Object t = obj.get("token");
			if (t != null) return String.valueOf(t);
			Object at = obj.get("access_token");
			if (at != null) return String.valueOf(at);
		} catch (Exception ignored) {}

		try {
			return MAPPER.readValue(body, String.class);
		} catch (Exception ignored) {}

		return body;
	}
	/**
	 * Attempts to parse a response body as a JSON object.
	 * 
	 * <p>If the body is not valid JSON or is not a JSON object,
	 * this method returns an empty map instead of throwing.
	 * 
	 * <p>This allows negative test cases to safely inspect error responses
	 * without crashing the test harness.
	 * 
	 * @param body the raw response body
	 * @return a map representing the JSON object, or an empty map on failure
	 */
	private static Map<String, Object> parseJsonObject(String body) {
		try {
			return MAPPER.readValue(body, new TypeReference<Map<String, Object>>() {});
		} catch (Exception ignored) {
			return Map.of();
		}
	}
}
