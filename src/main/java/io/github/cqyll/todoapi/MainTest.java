package io.github.cqyll.todoapi;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;
import io.github.cqyll.todoapi.config.AppConfig;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

public class MainTest {
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final HttpClient HTTP = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    private static final String BASE = "http://localhost:8080";

    // Must match your OAuthTokenService config (demo client)
    private static final String CLIENT_ID = "todo-web";
    private static final String CLIENT_SECRET = "todo-secret";

    public static void main(String[] args) throws Exception {
        HttpServer server = null;

        try {
            server = new AppConfig().createHttpServer();
            server.start();
            System.out.println("Server started on " + BASE);

            runTests();

        } finally {
            if (server != null) {
                server.stop(0);
                System.out.println("Server stopped.");
            }
        }
    }

    private static void runTests() throws Exception {
        System.out.println("\n== Console Tests ==");

        String email = "user" + System.currentTimeMillis() + "@example.com";
        String password = "Passw0rd!";
        String name = "Console Test";

        System.out.println("\n[1] Register user: POST /register");
        String regToken = register(email, name, password);
        System.out.println("Register returned token: " + summarizeToken(regToken));

        System.out.println("\n[2] Login JSON: POST /login");
        String loginToken = loginJson(email, password);
        System.out.println("Login returned token: " + summarizeToken(loginToken));

        System.out.println("\n[3] OAuth password grant (client_secret_basic): POST /oauth/token");
        System.out.println(pretty(oauthPasswordClientSecretBasic(email, password)));

        System.out.println("\n[4] OAuth password grant (client_secret_post): POST /oauth/token");
        System.out.println(pretty(oauthPasswordClientSecretPost(email, password)));

        System.out.println("\n[5] Negative: invalid_client (wrong secret) (expect 401 + error=invalid_client)");
        negativeInvalidClient(email, password);

        System.out.println("\n[6] Negative: invalid_grant (wrong password) (expect 400 + error=invalid_grant)");
        negativeInvalidGrant(email);

        System.out.println("\n[7] Negative: unsupported_grant_type (expect 400 + error=unsupported_grant_type)");
        negativeUnsupportedGrantType(email, password);

        System.out.println("\n== Done ==");
    }

    // -------------------- /register --------------------

    private static String register(String email, String name, String password) throws Exception {
        // Your current UserController expects: email, name, password
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("email", email);
        payload.put("name", name);
        payload.put("password", password);

        HttpResponse<String> r = postJson(BASE + "/register", payload);
        printResponse(r);

        if (r.statusCode() >= 400) {
            throw new RuntimeException("Register failed: " + r.body());
        }

        // Your current /register returns a JSON string token: "abc"
        // If you change it later to {token:"abc"}, this still works.
        return extractTokenAnyShape(r.body());
    }

    // -------------------- /login --------------------

    private static String loginJson(String email, String password) throws Exception {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("email", email);
        payload.put("password", password);

        HttpResponse<String> r = postJson(BASE + "/login", payload);
        printResponse(r);

        if (r.statusCode() != 200) {
            throw new RuntimeException("Login failed: " + r.body());
        }

        Map<String, Object> body = parseJsonObjectOrEmpty(r.body());
        Object token = body.get("token");
        if (token == null) return extractTokenAnyShape(r.body());
        return token.toString();
    }

    // -------------------- /oauth/token --------------------

    private static Map<String, Object> oauthPasswordClientSecretBasic(String username, String password) throws Exception {
        String form = form(Map.of(
                "grant_type", "password",
                "username", username,
                "password", password
        ));

        String basic = Base64.getEncoder().encodeToString(
                (CLIENT_ID + ":" + CLIENT_SECRET).getBytes(StandardCharsets.UTF_8)
        );

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/oauth/token"))
                .header("Authorization", "Basic " + basic)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(form))
                .build();

        HttpResponse<String> r = HTTP.send(req, HttpResponse.BodyHandlers.ofString());
        printResponse(r);

        if (r.statusCode() != 200) {
            throw new RuntimeException("OAuth token (basic) failed: " + r.body());
        }
        return parseJsonObjectOrEmpty(r.body());
    }

    private static Map<String, Object> oauthPasswordClientSecretPost(String username, String password) throws Exception {
        String form = form(Map.of(
                "grant_type", "password",
                "client_id", CLIENT_ID,
                "client_secret", CLIENT_SECRET,
                "username", username,
                "password", password
        ));
        // create http request
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/oauth/token"))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(form))
                .build();

        // use HTTP proxy from java.net to send request and save the response
        HttpResponse<String> r = HTTP.send(req, HttpResponse.BodyHandlers.ofString());
        printResponse(r);

        if (r.statusCode() != 200) {
            throw new RuntimeException("OAuth token (post) failed: " + r.body());
        }
        return parseJsonObjectOrEmpty(r.body());
    }

    // -------------------- Negatives --------------------

    private static void negativeInvalidClient(String username, String password) throws Exception {
        String form = form(Map.of(
                "grant_type", "password",
                "username", username,
                "password", password
        ));

        String basic = Base64.getEncoder().encodeToString(
                (CLIENT_ID + ":" + "WRONG_SECRET").getBytes(StandardCharsets.UTF_8)
        );

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/oauth/token"))
                .header("Authorization", "Basic " + basic)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(form))
                .build();

        HttpResponse<String> r = HTTP.send(req, HttpResponse.BodyHandlers.ofString());
        printResponse(r);

        Map<String, Object> body = parseJsonObjectOrEmpty(r.body());
        assertStatus(401, r.statusCode());
        assertEquals("invalid_client", String.valueOf(body.get("error")));
    }

    private static void negativeInvalidGrant(String username) throws Exception {
        String form = form(Map.of(
                "grant_type", "password",
                "username", username,
                "password", "WRONG_PASSWORD"
        ));

        String basic = Base64.getEncoder().encodeToString(
                (CLIENT_ID + ":" + CLIENT_SECRET).getBytes(StandardCharsets.UTF_8)
        );

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/oauth/token"))
                .header("Authorization", "Basic " + basic)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(form))
                .build();

        HttpResponse<String> r = HTTP.send(req, HttpResponse.BodyHandlers.ofString());
        printResponse(r);

        Map<String, Object> body = parseJsonObjectOrEmpty(r.body());
        assertStatus(400, r.statusCode());
        assertEquals("invalid_grant", String.valueOf(body.get("error")));
    }

    private static void negativeUnsupportedGrantType(String username, String password) throws Exception {
        // build form body using helper
    	String form = form(Map.of(
                "grant_type", "client_credentials",
                "username", username,
                "password", password
        ));
    	// build Authorization header
        String basic = Base64.getEncoder().encodeToString(
                (CLIENT_ID + ":" + CLIENT_SECRET).getBytes(StandardCharsets.UTF_8)
        );

        // create HTTP request
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/oauth/token"))
                .header("Authorization", "Basic " + basic)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(form))
                .build();

        HttpResponse<String> r = HTTP.send(req, HttpResponse.BodyHandlers.ofString());
        printResponse(r);

        Map<String, Object> body = parseJsonObjectOrEmpty(r.body());
        assertStatus(400, r.statusCode());
        assertEquals("unsupported_grant_type", String.valueOf(body.get("error")));
    }

    // -------------------- HTTP helpers --------------------

    private static HttpResponse<String> postJson(String url, Object payload) throws Exception {
        String json = MAPPER.writeValueAsString(payload);
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json; charset=utf-8")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
        return HTTP.send(req, HttpResponse.BodyHandlers.ofString());
    }

    private static String form(Map<String, String> params) {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (Map.Entry<String, String> e : params.entrySet()) {
            if (!first) sb.append('&');
            first = false;
            sb.append(enc(e.getKey())).append('=').append(enc(e.getValue()));
        }
        return sb.toString();
    }

    private static String enc(String s) {
        return URLEncoder.encode(s == null ? "" : s, StandardCharsets.UTF_8);
    }

    // -------------------- Parsing helpers --------------------

    private static Map<String, Object> parseJsonObjectOrEmpty(String body) {
        try {
            return MAPPER.readValue(body, new TypeReference<Map<String, Object>>() {});
        } catch (Exception ignored) {
            return Map.of();
        }
    }

    // Supports:
    // 1) JSON string token: "abc"
    // 2) JSON object token: {"token":"abc"}
    // 3) OAuth object: {"access_token":"abc", ...}
    private static String extractTokenAnyShape(String body) {
        if (body == null) return null;

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

    // -------------------- Output/assert helpers --------------------

    private static void printResponse(HttpResponse<String> r) {
        System.out.println("HTTP " + r.statusCode());
        if (r.body() == null || r.body().isBlank()) {
            System.out.println("(empty body)");
        } else {
            System.out.println(r.body());
        }
    }

    private static String pretty(Object o) {
        try {
            return MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(o);
        } catch (Exception e) {
            return String.valueOf(o);
        }
    }

    private static String summarizeToken(String token) {
        if (token == null) return "null";
        if (token.length() <= 24) return token;
        return token.substring(0, 10) + "..." + token.substring(token.length() - 6);
    }

    private static void assertEquals(String expected, String actual) {
        if (expected == null && actual == null) return;
        if (expected != null && expected.equals(actual)) return;
        throw new AssertionError("Expected [" + expected + "] but got [" + actual + "]");
    }

    private static void assertStatus(int expected, int actual) {
        if (expected == actual) return;
        throw new AssertionError("Expected HTTP " + expected + " but got HTTP " + actual);
    }
}