package io.github.cqyll.todoapi.adapter.inbound.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import io.github.cqyll.todoapi.application.port.inbound.OAuthTokenUseCase;
import io.github.cqyll.todoapi.dto.OAuthTokenRequest;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class OAuthTokenController implements HttpHandler {
    private static final ObjectMapper mapper = new ObjectMapper();
    private final OAuthTokenUseCase useCase;

    public OAuthTokenController(OAuthTokenUseCase useCase) {
        this.useCase = useCase;
    }

    @Override
    public void handle(HttpExchange ex) throws IOException {
        if (!"POST".equalsIgnoreCase(ex.getRequestMethod())) {
            ex.getResponseHeaders().set("Allow", "POST");
            ex.sendResponseHeaders(405, -1);
            return;
        }

        try {
            // RFC 6749 token endpoint: form parameters. We do NOT branch on Content-Type.
            String raw = new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            Map<String, String> form = parseForm(raw);

            // Client authentication: prefer Authorization header (client_secret_basic),
            // fallback to form (client_secret_post). If both provided, header wins.
            ClientAuth clientAuth = parseClientAuth(ex, form);

            OAuthTokenRequest req = new OAuthTokenRequest();
            req.setGrantType(form.get("grant_type"));
            req.setUsername(form.get("username"));
            req.setPassword(form.get("password"));
            req.setRefreshToken(form.get("refresh_token"));
            req.setScope(form.get("scope"));
            req.setRedirectUri(form.get("redirect_uri"));

            req.setClientId(clientAuth.clientId);
            req.setClientSecret(clientAuth.clientSecret);

            Map<String, Object> resp = useCase.token(req);

            // Token responses must be JSON and should not be cached (RFC 6749).
            ex.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
            ex.getResponseHeaders().set("Cache-Control", "no-store");
            ex.getResponseHeaders().set("Pragma", "no-cache");

            writeJson(ex, 200, resp);

        } catch (OAuthError e) {
            writeOAuthError(ex, e);

        } catch (Exception e) {
            // Spec: server_error
            writeOAuthError(ex, OAuthError.serverError());
        }
    }

    private void writeOAuthError(HttpExchange ex, OAuthError err) throws IOException {
        ex.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        ex.getResponseHeaders().set("Cache-Control", "no-store");
        ex.getResponseHeaders().set("Pragma", "no-cache");

        if (err.getHttpStatus() == 401) {
            // Typical for invalid_client. (RFC 6749 + RFC 6750 patterns)
            ex.getResponseHeaders().set("WWW-Authenticate", "Basic realm=\"oauth\"");
        }

        Map<String, Object> body = new HashMap<>();
        body.put("error", err.getError());
        if (err.getDescription() != null && !err.getDescription().isBlank()) {
            body.put("error_description", err.getDescription());
        }
        if (err.getUri() != null && !err.getUri().isBlank()) {
            body.put("error_uri", err.getUri());
        }

        writeJson(ex, err.getHttpStatus(), body);
    }

    private void writeJson(HttpExchange ex, int status, Object body) throws IOException {
        byte[] bytes = mapper.writeValueAsBytes(body);
        ex.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = ex.getResponseBody()) {
            os.write(bytes);
        }
    }

    private Map<String, String> parseForm(String body) {
        Map<String, String> params = new HashMap<>();
        if (body == null || body.isBlank()) return params;

        for (String pair : body.split("&")) {
            String[] kv = pair.split("=", 2);
            if (kv.length == 2) {
                String k = URLDecoder.decode(kv[0], StandardCharsets.UTF_8);
                String v = URLDecoder.decode(kv[1], StandardCharsets.UTF_8);
                params.put(k, v);
            }
        }
        return params;
    }

    private ClientAuth parseClientAuth(HttpExchange ex, Map<String, String> form) {
        String auth = ex.getRequestHeaders().getFirst("Authorization");
        if (auth != null && auth.startsWith("Basic ")) {
            String b64 = auth.substring("Basic ".length()).trim();
            String decoded = new String(Base64.getDecoder().decode(b64), StandardCharsets.UTF_8);
            int idx = decoded.indexOf(':');
            String id = idx >= 0 ? decoded.substring(0, idx) : decoded;
            String secret = idx >= 0 ? decoded.substring(idx + 1) : "";
            return new ClientAuth(id, secret);
        }

        // Fallback: client_secret_post
        String clientId = form.get("client_id");
        String clientSecret = form.get("client_secret");
        return new ClientAuth(clientId, clientSecret);
    }

    private static final class ClientAuth {
        final String clientId;
        final String clientSecret;

        ClientAuth(String clientId, String clientSecret) {
            this.clientId = clientId;
            this.clientSecret = clientSecret;
        }
    }
}
